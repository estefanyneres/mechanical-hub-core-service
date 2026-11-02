# language: pt
Funcionalidade: Fluxo completo do Saga da ordem de serviço

  Como mecânico e cliente da oficina, quero que a ordem de serviço
  percorra diagnóstico, aprovação do orçamento, pagamento e reserva de
  material até ficar pronta pra execução (RFC-001, seção 7 do plano).

  Cenário: Ordem de serviço avança do recebimento até ficar pronta pra execução
    Dado que uma ordem de serviço foi criada
    Quando o mecânico inicia o diagnóstico
    E o mecânico envia a ordem para aprovação
    Então o evento "orcamento.solicitado" deve ter sido publicado
    Quando o orçamento é gerado no valor de "350.00"
    Então a ordem deve estar com o orçamento atualizado para "350.00"
    Quando o cliente aprova o orçamento
    Então o status da ordem deve ser "APROVADO"
    Quando o pagamento é confirmado
    Então o status da ordem deve ser "PAGAMENTO_REALIZADO"
    Quando o material é reservado com sucesso
    Então o status da ordem deve ser "PRONTO_PARA_EXECUCAO"
    E a etapa do saga deve ser "AGUARDANDO_EXECUCAO"
