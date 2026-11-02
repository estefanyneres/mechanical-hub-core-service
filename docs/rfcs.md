# RFCs — Fase 4

RFCs documentam decisões técnicas relevantes tomadas durante o desenvolvimento
da fase 4, com o contexto, as alternativas consideradas e as consequências de
cada escolha. São cross-cutting (cobrem o sistema distribuído como um todo,
não um microsserviço isolado) — por isso vivem aqui, no `core-service`, que é
o orquestrador do fluxo.

---

## RFC-001 — Saga Pattern orquestrado, embutido no Core Service

**Contexto:** o PDF exige implementar o Saga Pattern para coordenar o fluxo
transacional da ordem de serviço (abrir OS → gerar orçamento → aguardar
aprovação → enviar para execução), com rollback/compensação em caso de falha
em qualquer etapa. Precisa ser escolhido entre **orquestrado** (um
orquestrador central decide os próximos passos) ou **coreografado** (cada
serviço reage a eventos dos outros, sem coordenação central).

**Decisão:** Saga orquestrada, com o orquestrador embutido no próprio
`core-service` — não um microsserviço à parte, nem um serviço gerenciado
externo (ex: AWS Step Functions).

**Discussão:**
- O `core-service` herda a entidade `service_orders` do monolito (onde a
  coluna de status existe desde a Fase 1) e passa a ser o dono conceitual do
  "estado geral" da ordem de serviço nesta fase. Faz sentido que o estado da
  saga more explicitamente numa tabela dele, em vez
  de ser **deduzido** juntando o status local dos 4 serviços — que é o que
  aconteceria numa coreografia pura (nenhum serviço tem a visão completa do
  "onde essa OS está agora").
- Coreografia pura foi descartada por exigir que cada serviço "saiba" reagir
  a eventos de falha dos outros de forma distribuída — mais elegante
  academicamente, mas mais arriscado de implementar corretamente dentro do
  prazo da fase, e mais difícil de testar o fluxo completo via BDD (exigiria
  subir os 4 serviços + a fila real pra observar o comportamento emergente).
- Um orquestrador separado (5º repositório só para coordenação) ou AWS Step
  Functions foram cogitados e descartados: o primeiro não reduz complexidade
  nenhuma, só a realoca; o segundo foi descartado por incerteza sobre
  permissão de IAM no ambiente AWS Academy (`LabRole`) e por acoplar a
  escolha de mensageria a SQS em vez de RabbitMQ.
- Orquestração não significa abrir mão de mensageria assíncrona: o
  `core-service` publica **comandos dirigidos** pra fila de cada serviço
  (ex: "gerar orçamento"), e cada serviço responde via reply/evento quando
  termina — continua tudo via RabbitMQ, nunca uma chamada HTTP síncrona
  bloqueante esperando minutos/dias por uma aprovação de cliente.

**Fluxo final de status (OrderStatusEnum) e eventos:**
- O Saga **não começa na criação da OS**: nasce só quando o mecânico decide
  manualmente que o diagnóstico acabou (`AGUARDANDO_APROVACAO`, via
  `PATCH /service-orders/{id}/status`) — não dá pra pedir orçamento antes de
  saber o que precisa ser feito. É só nesse momento que o `core-service`
  publica `orcamento.solicitado`.
- `APROVADO` (evento `orcamento.aprovado`) e `FALHA_PAGAMENTO` (evento
  `pagamento.falhou`) formam um ciclo: o cliente pode tentar pagar de novo
  quantas vezes precisar, e o próximo `pagamento.confirmado` resolve pra
  `PAGAMENTO_REALIZADO` a partir de qualquer um dos dois.
- `PAGAMENTO_REALIZADO` **não avança direto pra execução.** Pagamento
  confirmado é um fato diferente de "tem material disponível" — por isso
  existe um passo intermediário: o `repair-service` tenta reservar o
  material (síncrono com o Estoque Service, TASK-022) e só então publica
  `material.reservado` (→ `PRONTO_PARA_EXECUCAO`) ou `material.indisponivel`
  (→ `PENDENTE_ESTOQUE`).
- **`estoque.reabastecido` não é tratado pelo `core-service` diretamente.**
  Quando o `stock-service` publicar esse evento (material reposto), é o
  `repair-service` quem escuta e **tenta reservar de novo** — só então
  publica `material.reservado`/`material.indisponivel`, exatamente o mesmo
  evento de sempre. Isso evita que a OS seja marcada `PRONTO_PARA_EXECUCAO`
  só porque a peça chegou, sem ninguém de fato confirmar a reserva —
  `material.reservado` continua sendo o único evento que avança pra
  `PRONTO_PARA_EXECUCAO`, venha ele da primeira tentativa ou de um retry.
- `EM_EXECUCAO` **continua manual**, a partir de `PRONTO_PARA_EXECUCAO` —
  pagamento confirmado e material disponível não significam que o mecânico
  tem disponibilidade imediata pra começar; ele inicia quando puder, mesmo
  mecanismo manual já usado pra diagnóstico/aprovação/entrega.
- `FINALIZADO` continua vindo só do evento `execucao.finalizada`: quem sabe
  que todas as tarefas acabaram é o `repair-service` (dono de `order_tasks`
  nesta fase), não uma ação direta neste serviço.
- `execucao.falhou` (falha *durante* a execução já iniciada manualmente,
  diferente de `material.indisponivel`) manda a OS de volta pra
  `EM_DIAGNOSTICO` — reavaliação do zero, descartando o orçamento antigo
  (`budget` volta a `null`; o próximo ciclo de aprovação gera um
  `orcamento.gerado` novo). Cada tentativa de saga pela mesma OS gera sua
  própria linha de `SagaState` (não reaproveita a anterior, que fica
  marcada `FALHOU` como histórico). Junto com a reversão, o `core-service`
  publica `execucao.revertida` — o `stock-service` consome esse evento e
  libera **todo** o material que já tinha reservado pra essa OS (decisão
  consciente de liberar tudo em vez de editar só a diferença entre o
  diagnóstico antigo e o novo: editar a diferença exigiria o
  `repair-service` rastrear a lista de tarefas antes/depois da reversão e
  um endpoint de remover tarefa que não existe hoje — complexidade que não
  se justifica pra um caminho de recuperação de falha, não o fluxo
  principal).

**Consequências:**
- O `core-service` precisa de uma tabela própria de estado de saga
  (`saga_id`, `os_id`, `etapa_atual`), além dos dados de domínio que já
  possui (clientes, veículos, ordens de serviço).
- O `core-service` conhece a sequência completa do fluxo transacional — ele
  não é "mais um serviço de domínio", também concentra conhecimento de
  processo. Esse acoplamento é aceito conscientemente em troca de
  rastreabilidade/testabilidade.
- Se o `core-service` ficar indisponível, nenhuma saga avança (ponto único
  de coordenação) — mitigado com os mesmos mecanismos de alerta via Datadog
  já usados na Fase 3.
- Compensação/rollback de cada falha (orçamento recusado, pagamento
  falho, estoque insuficiente) é decidida num lugar só, facilitando tanto o
  teste BDD do fluxo completo quanto a demonstração em vídeo.
