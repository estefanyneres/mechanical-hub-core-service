# RFCs — Fase 3 (histórico)

> Migrado do repositório `mechanical-hub` (monolito das Fases 1-3), que não é
> mais utilizado a partir da Fase 4 — decisões mantidas aqui só como registro
> histórico, cobrindo a arquitetura de infraestrutura que continua válida
> (nuvem, banco relacional, autenticação, observabilidade). As decisões
> específicas da Fase 4 (Saga Pattern) estão em [`rfcs.md`](rfcs.md), neste
> mesmo repositório.

RFCs documentam decisões técnicas relevantes tomadas durante o desenvolvimento da fase 3, com o contexto, as alternativas consideradas e as consequências de cada escolha.

---

## RFC-001 — Escolha da nuvem e restrições do AWS Academy Learner Lab

**Contexto:** o desafio permite livre escolha de provedor de nuvem.

**Decisão:** AWS, via AWS Academy Learner Lab.

**Discussão:** o Learner Lab fornece uma conta AWS real e gratuita (50 doláres de crédito), porém com uma `LabRole` fixa e pré-provisionada, sem permissão para criar novas IAM Roles/Policies. Isso descartou, desde o início, qualquer abordagem que dependesse de criar roles novas (Cluster Autoscaler, OIDC Provider para GitHub Actions, Enhanced Monitoring do RDS).

**Consequências:**
- Sessões são efêmeras (~4h) e a conta é reiniciada com credenciais novas a cada sessão — todos os repositórios de infraestrutura têm um `pipeline.conf` com flag `DESTROY`, usado para derrubar tudo ao fim da sessão e recriar no início da próxima (o processo de autenticação de cada sessão nova é detalhado no RFC-005).
- Toda decisão de infraestrutura desta fase precisou ser filtrada pela pergunta "a LabRole permite isso?" antes de ser considerada viável — continua valendo na Fase 4 (ver ADR-007 do core-service, AWS Academy Learner Lab).

---

## RFC-002 — PostgreSQL relacional para o domínio de ordens de serviço

**Contexto:** o PDF exige um Banco de Dados Gerenciado, com escolha livre (PostgreSQL, MySQL, SQL Server etc.).

**Decisão:** PostgreSQL, via RDS.

**Discussão:** o domínio (clientes → veículos → ordens de serviço → tarefas → materiais/estoque) tem relacionamentos fortes e exige integridade referencial real (ex: não é possível excluir um veículo com ordens vinculadas — `ON DELETE RESTRICT`). Há necessidade de consistência transacional em operações como reserva de estoque e cálculo de orçamento. Não há requisito de escala horizontal ou schema flexível que justificasse uma solução NoSQL. RDS Postgres cabe dentro dos limites do Learner Lab (`db.t3.micro`, `gp2`, até 100GB).

**Consequências:**
- Constraints de FK ocasionalmente aparecem como erro genérico de infraestrutura quando falta validação de negócio na aplicação.
- Na Fase 4, essa mesma escolha se estendeu pra 3 dos 4 microsserviços novos (core-service, stock-service, billing-service); só o repair-service foge dessa regra, com DynamoDB (ver ADR-003 da Fase 4).

---

## RFC-003 — Estratégia de autenticação: dois fluxos coexistindo

**Contexto:** o PDF exige autenticação via CPF para rotas sensíveis do cliente. A aplicação já tinha, da fase 2, login interno (e-mail/senha) para a equipe (Administrador/Mecânico).

**Decisão:** manter os dois fluxos coexistindo, sem unificar.

**Discussão:**
- Equipe interna: login e-mail/senha → JWT com `subject = email`, emitido pela própria aplicação Spring.
- Cliente externo: CPF + placa → JWT com `subject = customerId` e claim `type: customer`, emitido pela Lambda de autenticação.

Fazem sentido separados porque atendem necessidades diferentes: a equipe precisa de acesso amplo, com papéis (roles) e credencial gerenciável; o cliente só precisa de autoatendimento restrito à própria ordem, sem necessidade de cadastro de senha — CPF + placa já é uma verificação de identidade suficiente para esse escopo estreito.

**Consequências:** o mesmo filtro de segurança da aplicação precisa diferenciar os dois tipos de token pela claim `type`, mapeando para principals diferentes, cada um com regras de autorização próprias. Na Fase 4, esse padrão se replicou: o billing-service tem seu próprio `CustomerSecurityFilter`/`CustomerJwtValidator` pra validar o JWT de cliente na aprovação/recusa de orçamento.

---

## RFC-004 — Lambda authorizer validando na borda, mantendo Spring Security como defesa em profundidade

**Contexto:** poderia-se confiar inteiramente no Lambda authorizer do API Gateway para barrar acesso indevido, deixando a aplicação aceitar tudo que chegasse.

**Decisão:** manter os dois níveis de validação — o authorizer na borda (API Gateway) e o Spring Security dentro da aplicação.

**Discussão:** o authorizer resolve uma pergunta grosseira ("esse token é válido, criptograficamente e não expirado?"). O Spring Security resolve perguntas finas que o authorizer não tem contexto de negócio para responder ("esse usuário tem papel de Administrador?", "essa ordem pertence a esse cliente?"). Camadas em profundidade também protegem contra o cenário em que a aplicação seja alcançada sem passar pelo API Gateway (por engano de configuração de rede).

**Consequências:** validação de assinatura/expiração do JWT existe, conceitualmente, duas vezes (authorizer e decode na aplicação), usando o mesmo segredo — duplicação aceita e intencional. É exatamente essa separação que permite validar que um token inválido é barrado no authorizer e nunca chega a acionar a aplicação. Mantido sem alteração na Fase 4 — os 4 microsserviços continuam atrás do mesmo authorizer.

---

## RFC-005 — Credenciais estáticas renovadas por sessão (OIDC inviável)

**Contexto:** a prática recomendada para autenticação de pipeline CI/CD com AWS é OIDC (GitHub Actions assume uma IAM Role via um Identity Provider OIDC, sem credencial de longa duração).

**Decisão:** usar as credenciais temporárias de sessão do AWS Academy, coladas manualmente nos GitHub Secrets.

**Discussão:** OIDC exige criar um IAM Identity Provider e uma IAM Role — ambos bloqueados pela `LabRole`, que não tem permissão para criar novas entidades IAM.

**Consequências:** as credenciais temporárias (prefixo `ASIA`, identificador padrão da AWS para credenciais de sessão via STS, diferente do prefixo `AKIA` usado em chaves permanentes de usuário IAM) precisam ser coladas manualmente nos GitHub Secrets a cada nova sessão — e como são vários repositórios de infraestrutura, cada um com seus próprios secrets de AWS, isso significa repetir essa atualização em todos eles sempre que a sessão do Lab reinicia. Processo manual, repetitivo e sujeito a erro (sem rotação automática), documentado nos READMEs dos repositórios de infraestrutura como parte do fluxo operacional esperado do ambiente. Continua assim na Fase 4, agora com 9 repositórios em vez de 6.

---

## RFC-006 — Escolha do Datadog

**Contexto:** o PDF permite escolha livre entre Datadog ou New Relic.

**Decisão:** Datadog.

**Discussão:** plataforma unificada para APM, logs estruturados, métricas customizadas (via Micrometer/StatsD), monitoramento de infraestrutura Kubernetes, dashboards e monitors/alertas — cobrindo todos os requisitos de observabilidade do PDF através de um único Agent instalado via Helm.

**Consequências:** contas de teste/laboratório têm duração limitada — durante o projeto, foi necessário migrar de uma conta trial expirando para uma conta fornecida por laboratório, exigindo recriar os dashboards exportando/importando, e trocar o secret `DD_API_KEY`. Essa experiência real foi o principal motivo para **não** adotar dashboards gerenciados via Terraform (ver ADR-013). Na Fase 4, o mesmo Agent (instalado uma única vez no `infra-k8s`) passou a monitorar os 4 microsserviços novos, e o dashboard foi adaptado (não recriado do zero) pra comparar métricas entre eles.

---

## RFC-007 — Ajustes no modelo relacional nesta fase

**Contexto:** o modelo relacional herdado da fase 2 já cobria clientes, veículos, ordens de serviço, catálogo de serviços/materiais e estoque. Dois requisitos novos da fase 3 não cabiam nesse modelo sem alteração.

**Decisão:** adicionar duas tabelas novas — `order_task_materials` e `service_order_status_history` — em vez de reaproveitar tabelas existentes.

**Discussão:**
- `order_task_materials`: antes, materiais só existiam vinculados ao catálogo do serviço (`service_materials`, a "receita padrão" de um serviço). Não havia como registrar quais materiais foram *de fato* consumidos numa tarefa (`order_task`) específica de uma OS real — necessário para rastrear consumo real por execução, que pode divergir da receita padrão (material trocado, quantidade diferente). Reaproveitar `service_materials` misturaria "catálogo" com "execução", perdendo o histórico de consumo por OS.
- `service_order_status_history`: antes, `service_orders` guardava apenas o status atual (`order_status`), sem histórico de quando cada transição ocorreu. Sem isso não é possível calcular o "tempo médio por status", nem correlacionar uma mudança de status com o trace/log daquele momento. A alternativa de derivar isso de logs foi descartada (ADR-012) por não ser uma fonte de dado confiável para métrica de negócio.

**Consequências:**
- `order_task_materials` referencia `order_tasks` com `ON DELETE CASCADE` (o consumo não faz sentido sem a tarefa) e `materials` com `ON DELETE RESTRICT`.
- `service_order_status_history` referencia `service_orders` com `ON DELETE CASCADE` (histórico não sobrevive à OS) e `users` com `ON DELETE RESTRICT`, nullable (transições automáticas do sistema não têm usuário responsável).
- Na Fase 4, esse modelo foi fatiado entre os microsserviços conforme o domínio (ver ADR-001 da Fase 4): `service_order_status_history` ficou no core-service, `order_task_materials` ficou no repair-service (como snapshot dentro do item DynamoDB, não como tabela separada).
