package com.fiap.mechanical_hub.core_service.domain.strategies.order_transition;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrderStatusHistory;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import com.fiap.mechanical_hub.core_service.domain.exceptions.InvalidOrderTransitionException;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.mocks.ServiceOrderMock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StartExecutionTransitionTest {

    private final ServiceOrderStatusHistoryGateway statusHistoryGateway = mock(ServiceOrderStatusHistoryGateway.class);
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final StartExecutionTransition transition = new StartExecutionTransition(statusHistoryGateway, registry);
    private final UUID mechanicId = UUID.randomUUID();

    @Test
    void shouldStartExecutionManuallyWhenReady() {
        ServiceOrder order = ServiceOrderMock.readyForExecutionOrder();

        transition.execute(order, mechanicId);

        assertEquals(OrderStatusEnum.EM_EXECUCAO, order.getStatus());
        verify(statusHistoryGateway).save(any(ServiceOrderStatusHistory.class));
    }

    @Test
    void shouldThrowExceptionWhenNotReadyForExecution() {
        ServiceOrder order = ServiceOrderMock.paymentRealizedOrder();

        assertThrows(InvalidOrderTransitionException.class, () -> transition.execute(order, mechanicId));
        verifyNoInteractions(statusHistoryGateway);
    }
}
