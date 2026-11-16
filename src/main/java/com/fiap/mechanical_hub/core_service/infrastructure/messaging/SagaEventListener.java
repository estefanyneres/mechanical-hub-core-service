package com.fiap.mechanical_hub.core_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mechanical_hub.core_service.application.usecases.SagaOrchestratorUseCase;
import com.fiap.mechanical_hub.core_service.domain.exceptions.InvalidOrderTransitionException;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

        try {
            dispatch(routingKey, serviceOrderId, body);
        } catch (InvalidOrderTransitionException e) {
            // Erro PERMANENTE de estado (nao vai ter sucesso em nenhuma
            // tentativa futura, ao contrario de uma falha transitoria tipo
            // banco fora do ar) -- descarta a mensagem so logando, em vez
            // de deixar o Spring AMQP reentregar ela pra sempre (TASK-046,
            // ja visto reentregando a ~500 msgs/s e consumindo CPU a toa).
            log.error("Transição de status inválida pra OS {} (routing key {}) - mensagem descartada: {}",
                    serviceOrderId, routingKey, e.getMessage());
        }
    }

    private void dispatch(String routingKey, UUID serviceOrderId, JsonNode body) {
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
            case RabbitMQConfig.ROUTING_KEY_EXECUCAO_INICIADA ->
                    orchestrator.onExecucaoIniciada(serviceOrderId);
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
