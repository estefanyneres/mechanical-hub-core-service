# ADRs — Fase 4

Architecture Decision Records: decisões arquiteturais permanentes tomadas
durante a fase 4. Cross-cutting (cobrem o sistema distribuído como um todo),
por isso vivem aqui no `core-service`, mesmo raciocínio das RFCs.

---

## ADR-001 — Decomposição em 4 microsserviços, por domínio de dados

**Status:** Aceito

**Contexto:** o PDF exige refatorar a aplicação monolítica em, no mínimo, 3
microsserviços independentes, cada um com repositório, infraestrutura e
banco de dados próprios — com a regra inegociável de que **nenhum serviço
pode acessar diretamente o banco de outro**. Era necessário decidir como
particionar as entidades do domínio existente (`customers`, `vehicles`,
`users`, `profiles`, `service_orders`, `order_tasks`, `services`,
`materials`, `stock`, `stock_movements`, `stock_pending_items`,
`service_order_status_history`) entre os novos serviços, de modo que cada
entidade tivesse um único dono.

**Decisão:** 4 microsserviços — um a mais que o mínimo exigido pelo PDF —
cada um dono exclusivo de um subconjunto de dados:

| Serviço | Dados que possui | Banco |
|---|---|---|
| `core-service` | `customers`, `vehicles`, `service_orders` (cabeçalho + status), `service_order_status_history`, `users`/`profiles` | PostgreSQL |
| `repair-service` | `order_tasks`, `services` (catálogo), `materials` (catálogo) | DynamoDB |
| `stock-service` | `stock`, `stock_movements`, `stock_pending_items` | PostgreSQL |
| `billing-service` | orçamentos, transações de pagamento (entidades novas, não migradas) | PostgreSQL |

**Consequências:**
- Nenhuma entidade tem dois donos — satisfaz a regra do PDF. Qualquer
  serviço que precise de dado de outro domínio consulta via API/evento, não
  via acesso direto ao banco.
- **Estoque virou o 4º serviço, não Identity/Cadastro**, por quatro motivos:
  tem uma âncora implícita no PDF ("Execução e Produção" pressupõe consumo
  de peça, mesmo sem citar estoque literalmente); já era a fronteira mais
  limpa que existia no código do monolito (`StockUseCase`,
  `StockMovementUseCase` e `StockPendingUseCase` já eram isolados, só se
  conectavam ao resto via um `service_order_id` solto pra auditoria); evita
  que `repair-service` virasse um serviço inchado (catálogo + materiais +
  estoque + tarefas todos juntos); e controle de estoque é uma capacidade de
  negócio genuinamente diferente de "executar um reparo". Identity/Cadastro
  foi descartado por não ter nenhuma âncora no texto do PDF e por não
  reduzir complexidade relevante de nenhum outro serviço.
- **Mudança de comportamento habilitada por essa separação:** no monolito, a
  reserva de estoque acontece na criação da OS (campo `has_stock_pending`).
  Com estoque separado, a reserva passa a ser adiada pra depois do pagamento
  confirmado — evita segurar peça reservada pra uma OS que ainda nem foi
  aprovada/paga (ver RFC-001, fluxo do Saga).
- `billing-service` não migra dados existentes do monolito — orçamento e
  pagamento são conceitos novos e isolados (hoje é só um campo `budget`
  solto em `service_orders`, sem tabela própria).
- `repair-service` é o único em DynamoDB — cumpre a exigência do PDF de ter
  pelo menos um banco SQL e pelo menos um NoSQL no conjunto dos serviços;
  dados de execução/diagnóstico são naturalmente semi-estruturados (cada
  reparo pode ter campos diferentes, notas variáveis, histórico de eventos).

---

## ADR-002 — Mensageria: RabbitMQ self-hosted no cluster EKS

**Status:** Aceito

**Contexto:** o PDF exige mensageria assíncrona (RabbitMQ, Kafka, SQS etc.)
para eventos e integração desacoplada entre os microsserviços — usada tanto
pelos comandos do orquestrador (ADR-001 das RFCs) quanto pelos
eventos/replies de resposta de cada serviço.

**Decisão:** RabbitMQ, self-hosted via Helm no mesmo cluster EKS já
existente (mesmo padrão já usado pro Datadog Agent e pro Cluster Autoscaler
na Fase 3).

**Discussão:**
- Evita repetir o processo de validar permissão de `LabRole` para mais um
  serviço gerenciado novo da AWS — já se sabe rodar e confiar em coisas
  via Helm no cluster existente.
- RabbitMQ tem suporte nativo ao padrão RPC sobre AMQP (fila de resposta
  correlacionada por ID), que encaixa diretamente no fluxo
  comando→reply que o orquestrador usa.
- **Kafka foi descartado**: é feito para outro problema (log de eventos
  duradouro, múltiplos consumidores independentes relendo um stream do
  zero), não para o request/reply pontual que o fluxo precisa. Também é
  operacionalmente mais pesado (Zookeeper/KRaft, mais memória/disco por
  broker) do que o node group pequeno do cluster comporta bem, e representa
  mais superfície de erro de infraestrutura numa sessão do AWS Academy que
  já é efêmera (~4h) por natureza.

**Consequências:**
- Se o orquestrador fosse migrado futuramente para AWS Step Functions,
  precisaria de uma Lambda-ponte (RabbitMQ não integra nativamente) — não é
  um risco ativo, já que essa migração foi descartada (RFC-001).

---

## ADR-003 — Banco não-relacional: DynamoDB

**Status:** Aceito

**Contexto:** o PDF exige pelo menos um banco não-relacional (NoSQL) no
conjunto dos microsserviços.

**Decisão:** DynamoDB (gerenciado pela AWS), usado pelo `repair-service`.

**Discussão:**
- Validado na documentação oficial do AWS Academy Learner Lab (atualizada
  em 24/06/2025): "Esse serviço pode assumir o perfil do IAM LabRole", sem
  nenhuma restrição de capacidade listada.
- É serverless — sobrevive ao ciclo de destroy/apply do AWS Academy sem
  custo por hora parado, diferente de um MongoDB self-hosted (opção
  descartada), que perderia os dados a cada sessão e ainda ocuparia espaço
  no node group o tempo todo.

**Consequências:**
- O `repair-service` passa a depender do SDK da AWS (não um driver genérico
  tipo MongoDB), acoplando esse serviço especificamente à AWS — aceito, já
  que o projeto inteiro já depende do AWS Academy (ADR-007).

---

## ADR-004 — SonarQube self-hosted, com banco H2 embutido

**Status:** Aceito

**Contexto:** o PDF exige validação de qualidade via SonarQube (ou similar)
no CI, com cobertura mínima de 80% por serviço bloqueando merge.

**Decisão:** SonarQube self-hosted (Helm no cluster EKS), usando o banco
**H2 embutido** — sem Postgres/RDS separado só para o Sonar.

**Discussão:**
- SonarCloud (alternativa gerenciada) só é gratuito para repositório
  público — self-hosted funciona independente da visibilidade do
  repositório.
- O H2 elimina a necessidade de um banco externo, já que não há interesse
  em manter histórico de qualidade entre sessões do AWS Academy (tudo é
  destruído e recriado de qualquer forma).
- **Risco aceito conscientemente:** a própria SonarSource documenta o H2
  como não recomendado para uso concorrente real — risco de lentidão ou
  falha no processamento em segundo plano se duas análises (de dois
  microsserviços diferentes) caírem muito próximas no tempo. Aceitável para
  a escala de um projeto de curso (4 repositórios, poucos commits por dia);
  na pior hipótese, exige rodar de novo um step que falhou.
- Precisa de uma rota pública nova no `api-gateway`, já que o servidor
  precisa ser alcançável pelos runners do GitHub Actions (que rodam fora da
  AWS). **Não precisa reconfigurar o Quality Gate via API a cada sessão**:
  a condição de 80% escolhida é sobre cobertura de **código novo**, não
  geral — e essa já é a condição padrão do gate "Sonar way", presente em
  qualquer banco H2 novo sem nenhuma chamada extra. O único passo que se
  repete por sessão é gerar um token de autenticação novo (automatizado via
  pipeline, TASK-011), já que esse sim é descartado a cada reset do H2.

**Consequências:**
- O gate de qualidade bloqueia o deploy (não só reporta), seguindo a ordem
  que o próprio PDF descreve: Build → Testes → Verificação de qualidade →
  Deploy.
- **PVC ligado no Helm chart, mesmo usando H2**: protege o arquivo do banco
  contra reinício do pod *dentro* da mesma sessão (node trocado pelo Cluster
  Autoscaler, OOM, um `helm upgrade` futuro) — sem isso, qualquer reinício no
  meio da sessão (não só o fim dela) zerava usuários, token e Quality Gate,
  com risco real de acontecer durante a gravação do vídeo de demonstração.
  Isso não contradiz a decisão de não persistir **entre** sessões: o volume
  é apagado junto quando o cluster EKS é destruído no fim da sessão
  (`DESTROY=true`), então o reset de usuários/token/Quality Gate
  (TASK-010/011) continua acontecendo uma vez por sessão nova, só não mais a
  qualquer reinício aleatório.

---

## ADR-005 — Todos os microsserviços em Java/Spring Boot

**Status:** Aceito

**Contexto:** era preciso decidir se os 4 microsserviços seriam todos na
mesma linguagem ou poliglotas, decisão que também afeta a escolha da
ferramenta de BDD (ADR-006).

**Decisão:** todos os 4 microsserviços em Java/Spring Boot.

**Discussão:** reaproveita diretamente o que já existe e já foi validado no
monolito da Fase 3 — JaCoCo, configuração do Sonar, `dd-java-agent`,
estrutura em Clean Architecture — reduzindo risco e esforço dado o prazo
apertado da fase (vale 90% da nota).

**Consequências:** a ferramenta de BDD pôde ser Cucumber-JVM (ADR-006) em
vez de uma ferramenta agnóstica de linguagem, que só se justificaria num
cenário poliglota.

---

## ADR-006 — BDD com Cucumber-JVM

**Status:** Aceito

**Contexto:** o PDF exige pelo menos um fluxo completo testado com BDD.

**Decisão:** Cucumber-JVM.

**Discussão:** como todos os serviços ficam em Java (ADR-005), o
Cucumber-JVM integra direto com o JUnit e o Spring Boot Test já usados hoje
— zero infraestrutura nova. **Karate foi descartado**: a vantagem dele
(testar qualquer serviço por HTTP, independente da linguagem que o
implementa) só compensaria num cenário poliglota, que não é o caso aqui.

**Consequências:** o fluxo escolhido para o teste BDD é o próprio Saga
fim-a-fim (abrir OS → orçamento aprovado → pagamento confirmado → enviado
para execução), cobrindo os 4 microsserviços num cenário só.

---

## ADR-007 — Continuidade no AWS Academy Learner Lab

**Status:** Aceito

**Contexto:** era preciso decidir se a Fase 4 continuaria no mesmo ambiente
da Fase 3 (AWS Academy Learner Lab, com as restrições de `LabRole` e
sessões de ~4h) ou migraria para outro provedor/conta.

**Decisão:** continuar no AWS Academy Learner Lab.

**Discussão:** reaproveita toda a infraestrutura compartilhada já validada
na Fase 3 (VPC, EKS, API Gateway, Lambda Authorizer, Datadog) — ponto de
partida muito mais rápido do que recriar tudo isso numa conta nova.

**Consequências:** todo novo serviço AWS tocado nesta fase (DynamoDB já
validado na ADR-003; demais a validar caso a caso) segue o mesmo filtro "a
`LabRole` permite isso?" já estabelecido na Fase 3. As mesmas restrições
operacionais conhecidas (sessões de ~4h, destroy/apply manual, impossível
criar IAM Roles novas) continuam valendo para os 4 novos microsserviços.
