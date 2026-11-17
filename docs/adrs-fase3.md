# ADRs — Fase 3 (histórico)

> Migrado do repositório `mechanical-hub` (monolito das Fases 1-3), que não é
> mais utilizado a partir da Fase 4 — decisões mantidas aqui só como registro
> histórico, cobrindo a arquitetura de infraestrutura que continua válida
> (VPC, EKS, ALB, API Gateway, autenticação, observabilidade). As decisões
> específicas da Fase 4 (microsserviços, Saga, RabbitMQ, DynamoDB) estão em
> [`adrs.md`](adrs.md), neste mesmo repositório.

Architecture Decision Records: decisões arquiteturais permanentes tomadas durante a fase 3, cobrindo toda a arquitetura do projeto.

---

## ADR-001 — `dd-java-agent` em vez de OpenTelemetry

**Status:** Aceito

**Contexto:** duas formas de instrumentar a JVM para tracing: `dd-java-agent` (agente proprietário do Datadog, auto-instrumentação) ou OpenTelemetry SDK/Collector exportando via OTLP.

**Decisão:** `dd-java-agent`, embarcado na imagem Docker (`ADD https://dtdg.co/latest-java-tracer dd-java-agent.jar`) e anexado via `-javaagent:`.

**Consequências:**
- Auto-instrumenta Tomcat, Spring MVC e JDBC/Hibernate sem código manual de spans.
- Integração nativa com correlação de logs (`dd.trace_id`) e métricas customizadas via StatsD — sem etapas extras de configuração.
- Lock-in ao Datadog como vendor de APM: trocar de fornecedor exigiria reinstrumentar a aplicação.

---

## ADR-002 — Descoberta entre repositórios via data source

**Status:** Aceito

**Contexto:** múltiplos repositórios Terraform (`infra-base`, `infra-database`, `infra-k8s`, `auth-lambda`, `api-gateway`) precisam referenciar recursos criados por outros repositórios (VPC, subnets, security groups, cluster EKS, instância RDS).

**Decisão:** cada repositório consome recursos de outros via `data source` do Terraform (filtrado por tag/nome), em vez de `terraform_remote_state` ou backend compartilhado.

**Consequências:**
- Mantém o state de cada repositório genuinamente independente, alinhado ao requisito de repositórios separados do PDF.
- Exige que o recurso de origem já exista no momento do apply — reforça a ordem obrigatória de execução (`base → database/k8s → auth-lambda → api-gateway`, e a reversa no destroy).
- Renomear/retaguear um recurso na origem quebra silenciosamente os `data source` downstream, só percebido em tempo de execução ("not found"), não em tempo de plan.

---

## ADR-003 — Security groups centralizados em `infra-base`

**Status:** Aceito

**Contexto:** os Security Groups (`alb`, `app`, `rds`, `lambda_auth`) são conceitualmente parte da infraestrutura de rede, usados por múltiplos repositórios.

**Decisão:** os 4 SGs são criados uma única vez em `infra-base`; os demais repositórios só consomem via `data source`, nunca criam SG próprio.

**Consequências:**
- **Incidente real desta fase:** o `infra-k8s` precisa adicionar sua própria regra de ingress no SG do RDS (liberando o SG automático do cluster EKS, cujo ID só existe depois que o cluster é criado — não pode viver em `infra-base`). Como o `infra-base` originalmente declarava as regras do RDS como blocos `ingress` inline (que o Terraform trata como lista autoritativa e completa), toda vez que o `infra-base` aplicava, ele apagava silenciosamente a regra que o `infra-k8s` tinha adicionado por fora — isso derrubou o acesso ao banco de dados da aplicação duas vezes na mesma sessão de trabalho.
- **Correção aplicada:** as regras do `infra-base` foram convertidas de blocos inline para recursos `aws_security_group_rule` separados, para que cada repositório gerencie apenas a(s) regra(s) que criou, sem conflitar com regras adicionadas por outros repositórios no mesmo SG.

---

## ADR-004 — ALB criado pelo Terraform, Service como NodePort

**Status:** Aceito

**Contexto:** duas formas de expor a aplicação dentro do EKS: um Service Kubernetes do tipo LoadBalancer (provisiona ALB/NLB automaticamente via AWS Load Balancer Controller) ou um Service NodePort com um ALB criado manualmente no Terraform.

**Decisão:** Service NodePort (porta fixa 30080) + ALB criado diretamente no Terraform (`infra-k8s`), apontando para os nodes do EKS nessa porta.

**Consequências:**
- Evita instalar e manter o AWS Load Balancer Controller (Helm chart extra + permissões IAM que a `LabRole` pode não suportar).
- O ALB é mantido **interno** (não exposto à internet), já que o ponto de entrada público real é o API Gateway, não o ALB.
- O target group do ALB precisa ser mantido em sincronia manual com o node group — aceitável dado o tamanho fixo e pequeno do node group neste projeto.

---

## ADR-005 — Separação de repositórios por taxa de mudança

**Status:** Aceito

**Contexto:** o PDF exige 4 repositórios separados no mínimo; o projeto usa 5 (um repositório dedicado para `api-gateway`, além de `infra-base`, `infra-database`, `infra-k8s`, `auth-lambda` e `mechanical-hub`).

**Decisão:** repositórios separados por frequência/independência de mudança, não estritamente por tipo de serviço AWS: `infra-base` (raramente muda), `infra-database` e `infra-k8s` (ciclos de vida independentes entre si), `auth-lambda` e `api-gateway` (evoluem junto com autenticação/roteamento, mas com granularidade própria), `mechanical-hub` (muda constantemente).

**Consequências:** mais repositórios para gerenciar (secrets, README, proteção de branch duplicados em cada um — rastreado via checklist próprio do projeto), mas cada pipeline roda rápido e cada `terraform plan` fica pequeno e revisável, sem misturar mudanças de camadas não relacionadas.

---

## ADR-006 — HPA e node group como duas camadas de escala; Cluster Autoscaler fora do escopo

**Status:** Aceito

**Contexto:** uma infraestrutura totalmente elástica usaria um Cluster Autoscaler (ou Karpenter) para adicionar/remover instâncias EC2 automaticamente, além do HPA escalar pods.

**Decisão:** o HPA escala pods (1 a 5 réplicas, alvo de 40% CPU / 60% memória); a capacidade do node group é fixada estaticamente via Terraform (`min_size=2, desired_size=2, max_size=5`), sem Cluster Autoscaler.

**Consequências:**
- O Cluster Autoscaler não foi instalado. **Importante:** consultando as regras oficiais do AWS Academy Learner Lab, o serviço EC2 Auto Scaling *pode* assumir o `LabRole`, e os nodes do EKS já rodam com o `LabRole` anexado como instance role — ou seja, a permissão de Auto Scaling em si provavelmente não seria um bloqueio. A restrição confirmada é outra: não é possível criar uma IAM Role nova (exceto roles vinculadas a serviço), o que bloquearia especificamente a forma padrão/recomendada de instalar o Cluster Autoscaler (via IRSA, que exige uma Role nova vinculada por OIDC ao cluster). Uma configuração alternativa, usando a permissão que o node já tem via `LabRole` diretamente (sem IRSA), provavelmente funcionaria, mas não foi testada dentro do prazo do projeto. A decisão de não instalar foi por tempo, não por uma restrição de IAM definitivamente comprovada.
- `max_size=5` no node group é "permissão para crescer", não automação: se um pod precisar ser agendado além da capacidade atual dos nodes, nada cria um node novo sozinho — com ou sem Cluster Autoscaler instalado.
- **Confirmado ao vivo nesta fase:** um pod ficou preso em `Pending` com "Insufficient memory" quando a aplicação precisou temporariamente de uma 5ª réplica durante um rolling deploy com apenas 2 nodes de pé.
- A demonstração em vídeo usa o HPA escalando pods sob carga de k6 (visível em segundos), já que o Cluster Autoscaler não funcionaria mesmo se houvesse tempo de demonstrá-lo.

---

## ADR-007 — Runtime da Lambda justificado pelo teto de simultaneidade

**Status:** Aceito

**Contexto:** a `auth-lambda` tem um teto de concorrência de 10 execuções simultâneas (limite de conta do AWS Academy Learner Lab, não configurado explicitamente pelo projeto) — acima disso, retorna 429.

**Decisão:** runtime Python 3.12 (`lambda.tf`: `runtime = "python3.12"`, `timeout = 10`, `memory_size = 256`).

**Consequências:** dado um teto de concorrência tão baixo, o tempo de cold start e de execução de cada invocação importa diretamente na probabilidade de esbarrar no limite sob uso concorrente — Python tem cold start consideravelmente mais rápido que runtimes baseados em JVM (como Java), o que minimiza a janela de tempo em que cada invocação ocupa uma das 10 vagas de concorrência disponíveis. Reforça também a decisão operacional de nunca gerar carga de teste (k6) diretamente contra `/auth/cpf`.

---

## ADR-008 — Nodes em subnet pública sem NAT Gateway

**Status:** Aceito

**Contexto:** nodes do EKS em subnet privada precisariam de um NAT Gateway para alcançar a internet (pull de imagens de registries públicos, chamadas a APIs da AWS).

**Decisão:** nodes do EKS ficam em subnets **públicas** (`infra-k8s/eks.tf`: `subnet_ids = data.aws_subnets.public.ids`), sem NAT Gateway provisionado.

**Consequências:**
- Evita o custo (cobrança por hora + por GB) e a complexidade de um NAT Gateway — relevante porque a infraestrutura é destruída e recriada várias vezes por dia nesse ambiente de laboratório.
- Nodes ficam com IP público, superfície de exposição maior que o desenho "de livro" (subnet privada + NAT) teria.
- O controle de acesso real continua sendo os Security Groups (o SG do RDS só aceita tráfego do SG da aplicação e da Lambda; o SG da aplicação só aceita a porta NodePort vinda do SG do ALB) — a subnet pública não significa que os nodes aceitam qualquer tráfego da internet.

---

## ADR-009 — Clean Architecture mantida da fase 2

**Status:** Aceito

**Contexto:** os requisitos da fase 3 (observabilidade, autenticação, infraestrutura) poderiam motivar uma reestruturação arquitetural.

**Decisão:** manter o particionamento em camadas já estabelecido na fase 2 (`domain/entities` → `application/gateways` + `usecases` → `infrastructure/{database,http,security,integrations,metrics}`).

**Consequências:**
- Os novos requisitos encaixam nas camadas existentes sem reestruturação (métricas/logging em `infrastructure`, nova regra de domínio em `ServiceOrder`, novo gateway para histórico de status).
- Um precedente pragmático já existente da fase 2 (classes de `domain/strategies` podem depender diretamente de interfaces de `application/gateways`) foi estendido nesta fase para também depender diretamente do `MeterRegistry` do Micrometer — uma exceção deliberada e documentada às regras estritas de dependência da Clean Architecture, justificada pelo escopo e prazo do projeto, não um descuido.

---

## ADR-010 — Migrations Flyway no repositório da aplicação

**Status:** Aceito

**Contexto:** as migrations do schema do banco poderiam viver no repositório `infra-database` (que provisiona a instância RDS) ou no repositório da aplicação.

**Decisão:** as migrations Flyway vivem no repositório da aplicação, executadas automaticamente na subida da aplicação. `infra-database` só provisiona a instância RDS vazia.

**Consequências:** o schema evolui junto com o código da aplicação, no mesmo commit/PR — mantendo os dois sempre em sincronia. `infra-database` não tem nenhum conhecimento do schema, o que se mostrou útil na prática: a instância RDS é destruída e recriada com muito mais frequência do que o schema muda, e toda instância nova é migrada automaticamente do zero até a versão mais recente assim que a aplicação sobe pela primeira vez (validado ao vivo múltiplas vezes nesta sessão, observando o Flyway rodar as migrations a partir de um schema vazio). Esse mesmo princípio foi mantido na Fase 4: cada microsserviço novo também traz suas próprias migrations.

---

## ADR-011 — Tabela de histórico de status

**Status:** Aceito

**Contexto:** o schema anterior guardava `order_status` como coluna única, sobrescrita a cada transição — sem forma de saber quanto tempo uma ordem passou em cada status intermediário.

**Decisão:** criar a tabela `service_order_status_history`, registrando cada transição (`from_status`, `to_status`, `changed_at`, `changed_by_user_id`).

**Consequências:** `openedAt`/`completedAt`/`deliveredAt` só davam 2 intervalos grosseiros, e `updated_at` não servia de proxy confiável (é tocado por mudanças de orçamento, tarefas e pendência de estoque, não só por transição de status). Cada estratégia de transição de status ganhou uma responsabilidade extra (registrar histórico + emitir a métrica de duração), centralizada em `ServiceOrderStatusTransitionRecorder` para não duplicar essa lógica nas classes de estratégia.

---

## ADR-012 — Métrica customizada em vez de derivação por log

**Status:** Aceito

**Contexto:** o painel "Volume diário de ordens de serviço" foi inicialmente implementado contando traces do APM (`trace.servlet.request.hits` filtrado por `POST /service-orders` e status 201).

**Decisão:** substituir por uma métrica de aplicação dedicada (`service_order.creation.count`, via `DistributionSummary` do Micrometer), incrementada explicitamente no código no momento em que uma ordem é criada com sucesso.

**Discussão:** o APM aplica amostragem (sampling) e retenção configurável sobre os traces — mesmo com baixo volume, não há garantia de que 100% das chamadas fiquem retidas e pesquisáveis, a menos que se configure uma Retention Filter específica para aquele endpoint. Contar traces funciona bem para análise estatística de latência/erros, mas não é uma fonte confiável para um número que precisa ser **exato** ("quantas ordens foram criadas hoje"). Uma métrica de aplicação, emitida deliberadamente no código de negócio, não depende de nenhuma decisão de amostragem do pipeline de observabilidade.

**Consequências:** todo dado de negócio que precisa ser contado com exatidão (não só amostrado) deve ser emitido como métrica de aplicação (Micrometer), não inferido a partir de contagem de trace — esse princípio deve ser seguido para futuras métricas de volume/contagem de negócio. Como efeito colateral dessa migração, foi necessário lidar com uma anomalia de publicação do `Counter` via StatsD (ver ADR-001) — resolvida adotando `DistributionSummary`, o mesmo mecanismo já validado pelo `service_order.status.duration`.

---

## ADR-013 — Dashboards mantidos via exportação/importação manual, não via Terraform

**Status:** Aceito

**Contexto:** o Datadog permite gerenciar dashboards e monitors como código, via os recursos `datadog_dashboard_json` e `datadog_monitor_json` do provider Terraform.

**Decisão:** manter os dashboards e monitors gerenciados manualmente via UI/Bits AI, exportando o JSON como cópia de segurança quando necessário, em vez de formalizar em Terraform.

**Discussão:** Terraform resolveria de forma automática um problema específico — widgets do tipo `alert_graph`/`alert_value` referenciam o ID numérico de um monitor, que muda entre contas Datadog diferentes (exigiria interpolação `datadog_monitor.x.id` dentro do JSON do dashboard). Esse problema só existiria de fato se o dashboard tivesse esse tipo de widget, o que não é o caso aqui. Adotar Terraform exigiria gerar uma `DD_APP_KEY` válida (distinta da `DD_API_KEY`, e que já foi confundida uma vez nesta fase), escrever a configuração `.tf`, e lidar com a mecânica de `templatefile()` para um JSON grande — custo real de engenharia para economizar um ajuste manual de poucos minutos, que só é necessário quando a conta do Datadog muda (algo raro: aconteceu uma única vez nesta fase, por expiração de um trial).

**Consequências:** ao trocar de conta Datadog, dashboards e monitors precisam ser recriados manualmente (reimportando o JSON exportado) — aceito como custo pontual e infrequente, documentado aqui como decisão consciente, não como lacuna.

---

## ADR-014 — Métrica de aplicação em vez de contagem de trace para eventos de negócio exatos

**Status:** Aceito

**Contexto:** o painel "Volume diário de ordens de serviço" foi inicialmente implementado contando traces do APM (`trace.servlet.request.hits` filtrado por `POST /service-orders` e status 201).

**Decisão:** substituir por uma métrica de aplicação dedicada (`service_order.creation.count`, via `DistributionSummary` do Micrometer), incrementada explicitamente no código no momento em que uma ordem é criada com sucesso.

**Discussão:** o APM aplica amostragem (sampling) e retenção configurável sobre os traces — mesmo com baixo volume, não há garantia de que 100% das chamadas fiquem retidas e pesquisáveis, a menos que se configure uma Retention Filter específica para aquele endpoint. Contar traces funciona bem para análise estatística de latência/erros, mas não é uma fonte confiável para um número que precisa ser **exato** ("quantas ordens foram criadas hoje"). Uma métrica de aplicação, emitida deliberadamente no código de negócio, não depende de nenhuma decisão de amostragem do pipeline de observabilidade.

**Consequências:** todo dado de negócio que precisa ser contado com exatidão (não só amostrado) deve ser emitido como métrica de aplicação (Micrometer), não inferido a partir de contagem de trace — esse princípio deve ser seguido para futuras métricas de volume/contagem de negócio, inclusive nos microsserviços da Fase 4. Como efeito colateral dessa migração, foi necessário lidar com uma anomalia de publicação do `Counter` via StatsD (ver ADR-001) — resolvida adotando `DistributionSummary`, o mesmo mecanismo já validado pelo `service_order.status.duration`.

---

## ADR-015 — Filtragem deliberada de ruído de observabilidade

**Status:** Aceito

**Contexto:** o Kubernetes chama `GET /actuator/health` periodicamente (liveness/readiness probe) para decidir se o pod está saudável, gerando um volume constante de traces que não representam tráfego de usuário real. Da mesma forma, o próprio Datadog Agent e componentes do `kube-system` (como o `kube-proxy`) geram logs internos de infraestrutura que não são logs da aplicação.

**Decisão:** filtrar esses dois ruídos deliberadamente:
- `DD_APM_IGNORE_RESOURCES` no Agent, ignorando o resource `GET /actuator/health` nos traces de APM.
- `containerExcludeLogs` no Agent, excluindo os namespaces `datadog` e `kube-system` da coleta de logs.

**Consequências:** a aplicação perde visibilidade **via trace** sobre o healthcheck (não é mais possível ver, no APM, quantas vezes `/actuator/health` foi chamado ou seu tempo de resposta). Essa perda é aceita conscientemente porque o requisito de "healthchecks e uptime" do PDF já é atendido de forma mais direta e confiável pelo estado do Kubernetes (`kubernetes_state.pod.ready`, que é literalmente o resultado do healthcheck passando ou não) — não há necessidade real do dado duplicado via trace. Sem essa filtragem, tanto as telas de Traces quanto as de Logs ficariam dominadas por ruído de infraestrutura, dificultando encontrar o que interessa (chamadas reais de usuário e logs da aplicação) durante o vídeo de demonstração.
