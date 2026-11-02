package com.fiap.mechanical_hub.core_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mechanical_hub.core_service.application.usecases.SagaOrchestratorUseCase;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Escuta a fila compartilhada (varios routing keys - ver contrato
 * completo em RabbitMQConfig) e decide o que fazer pelo routing key
 * recebido, lendo o JSON bruto -- de proposito, pra nao depender de
 * billing-service/repair-service (que ainda nao existem) setarem algum
 * header de tipo Java especifico do core-service. Eles so precisam
 * publicar JSON com os campos documentados em RabbitMQConfig.
 */
@Component
public class SagaEventListener {

    private final SagaOrchestratorUseCase orchestrator;
    private final ObjectMapper objectMapper;

    public SagaEventListener(SagaOrchestratorUseCase orchestrator, ObjectMapper objectMapper) {
        this.orchestrator = orchestrator;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.CORE_SERVICE_QUEUE)
    public void onMessage(Message message) throws IOException {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        JsonNode body = objectMapper.readTree(message.getBody());
        UUID serviceOrderId = UUID.fromString(body.get("serviceOrderId").asText());

        switch (routingKey) {
            case RabbitMQConfig.ROUTING_KEY_ORCAMENTO_GERADO -> {
                BigDecimal valor = new BigDecimal(body.get("valor").asText());
                orchestrator.onOrcamentoGerado(serviceOrderId, valor);
            }
            case RabbitMQConfig.ROUTING_KEY_ORCAMENTO_APROVADO ->
                    orchestrator.onOrcamentoAprovado(serviceOrderId);
            case RabbitMQConfig.ROUTING_KEY_ORCAMENTO_RECUSADO ->
                    orchestrator.onOrcamentoRecusado(serviceOrderId, textOrNull(body, "motivo"));
            case RabbitMQConfig.ROUTING_KEY_PAGAMENTO_CONFIRMADO ->
                    orchestrator.onPagamentoConfirmado(serviceOrderId);
            case RabbitMQConfig.ROUTING_KEY_PAGAMENTO_FALHOU ->
                    orchestrator.onPagamentoFalhou(serviceOrderId, textOrNull(body, "motivo"));
            case RabbitMQConfig.ROUTING_KEY_MATERIAL_RESERVADO ->
                    orchestrator.onMaterialReservado(serviceOrderId);
            case RabbitMQConfig.ROUTING_KEY_MATERIAL_INDISPONIVEL ->
                    orchestrator.onMaterialIndisponivel(serviceOrderId, textOrNull(body, "motivo"));
            case RabbitMQConfig.ROUTING_KEY_EXECUCAO_FINALIZADA ->
                    orchestrator.onExecucaoFinalizada(serviceOrderId);
            case RabbitMQConfig.ROUTING_KEY_EXECUCAO_FALHOU ->
                    orchestrator.onExecucaoFalhou(serviceOrderId, textOrNull(body, "motivo"));
            default -> throw new IllegalArgumentException("Routing key não reconhecida: " + routingKey);
        }
    }

    private String textOrNull(JsonNode body, String field) {
        JsonNode node = body.get(field);
        return node == null ? null : node.asText();
    }
}
