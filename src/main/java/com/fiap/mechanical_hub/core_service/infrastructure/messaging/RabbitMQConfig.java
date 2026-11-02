package com.fiap.mechanical_hub.core_service.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exchange/fila/bindings do Saga (RFC-001), declaradas via Spring AMQP no
 * proprio startup deste servico - padrao idiomatico do RabbitMQ (ver
 * decisao na TASK-006/discussao do infra-k8s).
 *
 * Contrato de routing keys (billing-service/repair-service precisam
 * usar exatamente estes nomes quando forem implementados - TASK-022/033):
 *
 *   core-service PUBLICA:
 *     - "orcamento.solicitado"  {serviceOrderId, orderNumber, requestDescription}
 *                               (billing-service consome, TASK-033. Disparado só
 *                               quando o mecânico move a OS manualmente pra
 *                               AGUARDANDO_APROVACAO via PATCH /service-orders/{id}/status
 *                               - não na criação da OS, não dá pra orçar antes do
 *                               diagnóstico existir)
 *     - "execucao.revertida"    {serviceOrderId}
 *                               (stock-service consome, TASK-028 - publicado junto com
 *                               a reversão pra EM_DIAGNOSTICO em onExecucaoFalhou, avisa
 *                               pra liberar o material que já tinha sido reservado pra
 *                               essa OS, já que o novo diagnóstico pode pedir peças
 *                               diferentes - decisão consciente de liberar tudo em vez de
 *                               editar só a diferença, ver RFC-001)
 *
 *   core-service CONSOME (fila core-service.eventos.queue, roteada por
 *   routing key no SagaEventListener - nao pelo tipo Java do payload, de
 *   proposito, pra nao acoplar o formato de mensagem aos DTOs internos
 *   de outro servico):
 *     - "orcamento.gerado"      {serviceOrderId, valor}            (billing-service)
 *     - "orcamento.aprovado"    {serviceOrderId}                   (billing-service)
 *     - "orcamento.recusado"    {serviceOrderId, motivo}           (billing-service)
 *     - "pagamento.confirmado"  {serviceOrderId}                   (billing-service - repair-service TAMBEM
 *                                                                    escuta esse mesmo evento direto, pra
 *                                                                    tentar reservar material - TASK-022.
 *                                                                    Fan-out via exchange, cada servico com
 *                                                                    a propria fila. So move a OS pra
 *                                                                    PAGAMENTO_REALIZADO -- NAO pra
 *                                                                    execucao direto, isso depende do
 *                                                                    resultado da reserva de material)
 *     - "pagamento.falhou"      {serviceOrderId, motivo}           (billing-service)
 *     - "material.reservado"    {serviceOrderId}                   (repair-service, TASK-022 - reserva de
 *                                                                    material OK, feita ANTES da execucao
 *                                                                    manual comecar. Unico evento que leva
 *                                                                    a PRONTO_PARA_EXECUCAO: o repair-service
 *                                                                    publica isso tanto na primeira tentativa
 *                                                                    (apos pagamento.confirmado) quanto numa
 *                                                                    nova tentativa depois de escutar
 *                                                                    estoque.reabastecido ele mesmo -- o
 *                                                                    core-service NAO reage a
 *                                                                    estoque.reabastecido diretamente, so
 *                                                                    confia numa reserva reconfirmada)
 *     - "material.indisponivel" {serviceOrderId, motivo}           (repair-service, TASK-022 - faltou peca
 *                                                                    na reserva sincrona com o Estoque Service)
 *     - "execucao.finalizada"   {serviceOrderId}                   (repair-service, TASK-024 - todas as
 *                                                                    tarefas concluidas)
 *     - "execucao.falhou"       {serviceOrderId, motivo}           (repair-service, TASK-024 - falha DURANTE
 *                                                                    a execucao manual ja iniciada, nao mais
 *                                                                    sobre falta de material -- isso agora e
 *                                                                    "material.indisponivel". Volta a OS pra
 *                                                                    EM_DIAGNOSTICO (reavaliacao do zero,
 *                                                                    orcamento antigo descartado) - ver
 *                                                                    RevertToDiagnosisTransition)
 */
@Configuration
public class RabbitMQConfig {

    public static final String SAGA_EXCHANGE = "saga.exchange";
    public static final String CORE_SERVICE_QUEUE = "core-service.eventos.queue";

    public static final String ROUTING_KEY_ORCAMENTO_SOLICITADO = "orcamento.solicitado";
    public static final String ROUTING_KEY_EXECUCAO_REVERTIDA = "execucao.revertida";
    public static final String ROUTING_KEY_ORCAMENTO_GERADO = "orcamento.gerado";
    public static final String ROUTING_KEY_ORCAMENTO_APROVADO = "orcamento.aprovado";
    public static final String ROUTING_KEY_ORCAMENTO_RECUSADO = "orcamento.recusado";
    public static final String ROUTING_KEY_PAGAMENTO_CONFIRMADO = "pagamento.confirmado";
    public static final String ROUTING_KEY_PAGAMENTO_FALHOU = "pagamento.falhou";
    public static final String ROUTING_KEY_MATERIAL_RESERVADO = "material.reservado";
    public static final String ROUTING_KEY_MATERIAL_INDISPONIVEL = "material.indisponivel";
    public static final String ROUTING_KEY_EXECUCAO_FINALIZADA = "execucao.finalizada";
    public static final String ROUTING_KEY_EXECUCAO_FALHOU = "execucao.falhou";

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(SAGA_EXCHANGE, true, false);
    }

    @Bean
    public Queue coreServiceQueue() {
        return new Queue(CORE_SERVICE_QUEUE, true);
    }

    @Bean
    public Binding orcamentoGeradoBinding(Queue coreServiceQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(coreServiceQueue).to(sagaExchange).with(ROUTING_KEY_ORCAMENTO_GERADO);
    }

    @Bean
    public Binding orcamentoAprovadoBinding(Queue coreServiceQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(coreServiceQueue).to(sagaExchange).with(ROUTING_KEY_ORCAMENTO_APROVADO);
    }

    @Bean
    public Binding orcamentoRecusadoBinding(Queue coreServiceQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(coreServiceQueue).to(sagaExchange).with(ROUTING_KEY_ORCAMENTO_RECUSADO);
    }

    @Bean
    public Binding pagamentoConfirmadoBinding(Queue coreServiceQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(coreServiceQueue).to(sagaExchange).with(ROUTING_KEY_PAGAMENTO_CONFIRMADO);
    }

    @Bean
    public Binding pagamentoFalhouBinding(Queue coreServiceQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(coreServiceQueue).to(sagaExchange).with(ROUTING_KEY_PAGAMENTO_FALHOU);
    }

    @Bean
    public Binding materialReservadoBinding(Queue coreServiceQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(coreServiceQueue).to(sagaExchange).with(ROUTING_KEY_MATERIAL_RESERVADO);
    }

    @Bean
    public Binding materialIndisponivelBinding(Queue coreServiceQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(coreServiceQueue).to(sagaExchange).with(ROUTING_KEY_MATERIAL_INDISPONIVEL);
    }

    @Bean
    public Binding execucaoFinalizadaBinding(Queue coreServiceQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(coreServiceQueue).to(sagaExchange).with(ROUTING_KEY_EXECUCAO_FINALIZADA);
    }

    @Bean
    public Binding execucaoFalhouBinding(Queue coreServiceQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(coreServiceQueue).to(sagaExchange).with(ROUTING_KEY_EXECUCAO_FALHOU);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
                                          MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
