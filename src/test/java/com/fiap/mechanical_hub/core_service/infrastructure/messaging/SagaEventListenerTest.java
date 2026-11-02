package com.fiap.mechanical_hub.core_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mechanical_hub.core_service.application.usecases.SagaOrchestratorUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SagaEventListenerTest {

    @Mock
    private SagaOrchestratorUseCase orchestrator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SagaEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new SagaEventListener(orchestrator, objectMapper);
    }

    private Message message(String routingKey, String json) {
        MessageProperties properties = new MessageProperties();
        properties.setReceivedRoutingKey(routingKey);
        return new Message(json.getBytes(StandardCharsets.UTF_8), properties);
    }

    @Test
    void shouldDispatchOrcamentoGerado() throws Exception {
        UUID orderId = UUID.randomUUID();
        listener.onMessage(message(RabbitMQConfig.ROUTING_KEY_ORCAMENTO_GERADO,
                "{\"serviceOrderId\":\"" + orderId + "\",\"valor\":\"500.00\"}"));

        verify(orchestrator).onOrcamentoGerado(orderId, new BigDecimal("500.00"));
    }

    @Test
    void shouldDispatchOrcamentoAprovado() throws Exception {
        UUID orderId = UUID.randomUUID();
        listener.onMessage(message(RabbitMQConfig.ROUTING_KEY_ORCAMENTO_APROVADO,
                "{\"serviceOrderId\":\"" + orderId + "\"}"));

        verify(orchestrator).onOrcamentoAprovado(orderId);
    }

    @Test
    void shouldDispatchOrcamentoRecusado() throws Exception {
        UUID orderId = UUID.randomUUID();
        listener.onMessage(message(RabbitMQConfig.ROUTING_KEY_ORCAMENTO_RECUSADO,
                "{\"serviceOrderId\":\"" + orderId + "\",\"motivo\":\"sem saldo\"}"));

        verify(orchestrator).onOrcamentoRecusado(orderId, "sem saldo");
    }

    @Test
    void shouldDispatchPagamentoConfirmado() throws Exception {
        UUID orderId = UUID.randomUUID();
        listener.onMessage(message(RabbitMQConfig.ROUTING_KEY_PAGAMENTO_CONFIRMADO,
                "{\"serviceOrderId\":\"" + orderId + "\"}"));

        verify(orchestrator).onPagamentoConfirmado(orderId);
    }

    @Test
    void shouldDispatchPagamentoFalhou() throws Exception {
        UUID orderId = UUID.randomUUID();
        listener.onMessage(message(RabbitMQConfig.ROUTING_KEY_PAGAMENTO_FALHOU,
                "{\"serviceOrderId\":\"" + orderId + "\",\"motivo\":\"cartão recusado\"}"));

        verify(orchestrator).onPagamentoFalhou(orderId, "cartão recusado");
    }

    @Test
    void shouldDispatchMaterialReservado() throws Exception {
        UUID orderId = UUID.randomUUID();
        listener.onMessage(message(RabbitMQConfig.ROUTING_KEY_MATERIAL_RESERVADO,
                "{\"serviceOrderId\":\"" + orderId + "\"}"));

        verify(orchestrator).onMaterialReservado(orderId);
    }

    @Test
    void shouldDispatchMaterialIndisponivel() throws Exception {
        UUID orderId = UUID.randomUUID();
        listener.onMessage(message(RabbitMQConfig.ROUTING_KEY_MATERIAL_INDISPONIVEL,
                "{\"serviceOrderId\":\"" + orderId + "\",\"motivo\":\"sem peça\"}"));

        verify(orchestrator).onMaterialIndisponivel(orderId, "sem peça");
    }

    @Test
    void shouldDispatchExecucaoFinalizada() throws Exception {
        UUID orderId = UUID.randomUUID();
        listener.onMessage(message(RabbitMQConfig.ROUTING_KEY_EXECUCAO_FINALIZADA,
                "{\"serviceOrderId\":\"" + orderId + "\"}"));

        verify(orchestrator).onExecucaoFinalizada(orderId);
    }

    @Test
    void shouldDispatchExecucaoFalhou() throws Exception {
        UUID orderId = UUID.randomUUID();
        listener.onMessage(message(RabbitMQConfig.ROUTING_KEY_EXECUCAO_FALHOU,
                "{\"serviceOrderId\":\"" + orderId + "\",\"motivo\":\"peça quebrou\"}"));

        verify(orchestrator).onExecucaoFalhou(orderId, "peça quebrou");
    }

    @Test
    void shouldThrowForUnrecognizedRoutingKey() {
        UUID orderId = UUID.randomUUID();
        Message msg = message("rota.desconhecida", "{\"serviceOrderId\":\"" + orderId + "\"}");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> listener.onMessage(msg));
    }
}
