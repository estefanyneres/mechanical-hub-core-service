package com.fiap.mechanical_hub.core_service.domain.strategies.order_transition;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrderStatusHistory;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import com.fiap.mechanical_hub.core_service.domain.exceptions.InvalidOrderTransitionException;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.mocks.ServiceOrderMock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConfirmPaymentTransitionTest {

    private final ServiceOrderStatusHistoryGateway statusHistoryGateway = mock(ServiceOrderStatusHistoryGateway.class);
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final ConfirmPaymentTransition transition = new ConfirmPaymentTransition(statusHistoryGateway, registry);

    @Test
    void shouldConfirmPaymentFromApproved() {
        ServiceOrder order = ServiceOrderMock.approvedOrder();

        transition.execute(order, null);

        assertEquals(OrderStatusEnum.PAGAMENTO_REALIZADO, order.getStatus());
        verify(statusHistoryGateway).save(any(ServiceOrderStatusHistory.class));
    }

    @Test
    void shouldConfirmPaymentFromFailedPaymentRetry() {
        ServiceOrder order = ServiceOrderMock.failedPaymentOrder();

        transition.execute(order, null);

        assertEquals(OrderStatusEnum.PAGAMENTO_REALIZADO, order.getStatus());
        verify(statusHistoryGateway).save(any(ServiceOrderStatusHistory.class));
    }

    @Test
    void shouldThrowExceptionWhenStatusIsInvalid() {
        ServiceOrder order = ServiceOrderMock.receivedOrder();

        assertThrows(InvalidOrderTransitionException.class, () -> transition.execute(order, null));
        verifyNoInteractions(statusHistoryGateway);
    }
}
