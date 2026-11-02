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

class StockPendingTransitionTest {

    private final ServiceOrderStatusHistoryGateway statusHistoryGateway = mock(ServiceOrderStatusHistoryGateway.class);
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final StockPendingTransition transition = new StockPendingTransition(statusHistoryGateway, registry);

    @Test
    void shouldMarkStockPendingFromPaymentRealized() {
        ServiceOrder order = ServiceOrderMock.paymentRealizedOrder();

        transition.execute(order, null);

        assertEquals(OrderStatusEnum.PENDENTE_ESTOQUE, order.getStatus());
        verify(statusHistoryGateway).save(any(ServiceOrderStatusHistory.class));
    }

    @Test
    void shouldThrowExceptionWhenStatusIsInvalid() {
        ServiceOrder order = ServiceOrderMock.receivedOrder();

        assertThrows(InvalidOrderTransitionException.class, () -> transition.execute(order, null));
        verifyNoInteractions(statusHistoryGateway);
    }
}
