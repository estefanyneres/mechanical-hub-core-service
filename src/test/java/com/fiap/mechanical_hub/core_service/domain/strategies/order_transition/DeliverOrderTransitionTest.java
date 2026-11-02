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

class DeliverOrderTransitionTest {

    private final ServiceOrderStatusHistoryGateway statusHistoryGateway = mock(ServiceOrderStatusHistoryGateway.class);
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final DeliverOrderTransition transition = new DeliverOrderTransition(statusHistoryGateway, registry);
    private final UUID userId = UUID.randomUUID();

    @Test
    void shouldDeliverOrderWhenStatusIsFinished() {
        ServiceOrder order = ServiceOrderMock.finishedOrder();

        transition.execute(order, userId);

        assertEquals(OrderStatusEnum.ENTREGUE, order.getStatus());
        assertNotNull(order.getDeliveredAt());
        verify(statusHistoryGateway).save(any(ServiceOrderStatusHistory.class));
    }

    @Test
    void shouldThrowExceptionWhenDeliveringFromNonFinishedStatus() {
        ServiceOrder order = ServiceOrderMock.inExecutionOrder();

        assertThrows(InvalidOrderTransitionException.class, () -> transition.execute(order, userId));
        verifyNoInteractions(statusHistoryGateway);
    }
}
