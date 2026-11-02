package com.fiap.mechanical_hub.core_service.domain.strategies.order_transition;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OrderStatusTransitionFactoryTest {

    @Test
    void shouldReturnRegisteredTransitionForTargetStatus() {
        StartDiagnosisTransition startDiagnosisTransition =
                new StartDiagnosisTransition(mock(ServiceOrderStatusHistoryGateway.class), mock(MeterRegistry.class));
        OrderStatusTransitionFactory factory = new OrderStatusTransitionFactory(
                Map.of(OrderStatusEnum.EM_DIAGNOSTICO, startDiagnosisTransition));

        OrderStatusTransition transition = factory.get(OrderStatusEnum.EM_DIAGNOSTICO);

        assertSame(startDiagnosisTransition, transition);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTransitionIsNotRegistered() {
        OrderStatusTransitionFactory factory = new OrderStatusTransitionFactory(Map.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> factory.get(OrderStatusEnum.EM_DIAGNOSTICO));

        assertEquals("Transição de status não suportada", exception.getMessage());
    }
}
