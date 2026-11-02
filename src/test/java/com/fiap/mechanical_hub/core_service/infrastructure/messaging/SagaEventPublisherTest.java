package com.fiap.mechanical_hub.core_service.infrastructure.messaging;

import com.fiap.mechanical_hub.core_service.application.dto.saga.ExecucaoRevertidaEvent;
import com.fiap.mechanical_hub.core_service.application.dto.saga.OrcamentoSolicitadoEvent;
import com.fiap.mechanical_hub.core_service.domain.entities.mocks.ServiceOrderMock;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SagaEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SagaEventPublisher publisher;

    @Test
    void shouldPublishOrcamentoSolicitadoEvent() {
        ServiceOrder order = ServiceOrderMock.defaultServiceOrder();

        publisher.publishOrcamentoSolicitado(order);

        ArgumentCaptor<OrcamentoSolicitadoEvent> captor = ArgumentCaptor.forClass(OrcamentoSolicitadoEvent.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq(RabbitMQConfig.SAGA_EXCHANGE),
                org.mockito.ArgumentMatchers.eq(RabbitMQConfig.ROUTING_KEY_ORCAMENTO_SOLICITADO),
                captor.capture());

        assertEquals(order.getId(), captor.getValue().serviceOrderId());
        assertEquals(order.getOrderNumber(), captor.getValue().orderNumber());
    }

    @Test
    void shouldPublishExecucaoRevertidaEvent() {
        UUID serviceOrderId = UUID.randomUUID();

        publisher.publishExecucaoRevertida(serviceOrderId);

        ArgumentCaptor<ExecucaoRevertidaEvent> captor = ArgumentCaptor.forClass(ExecucaoRevertidaEvent.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq(RabbitMQConfig.SAGA_EXCHANGE),
                org.mockito.ArgumentMatchers.eq(RabbitMQConfig.ROUTING_KEY_EXECUCAO_REVERTIDA),
                captor.capture());

        assertEquals(serviceOrderId, captor.getValue().serviceOrderId());
    }
}
