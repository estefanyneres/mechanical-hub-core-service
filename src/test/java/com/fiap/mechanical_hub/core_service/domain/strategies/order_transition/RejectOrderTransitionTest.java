package com.fiap.mechanical_hub.core_service.domain.strategies.order_transition;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrderStatusHistory;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import com.fiap.mechanical_hub.core_service.domain.exceptions.InvalidOrderTransitionException;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.mocks.ServiceOrderMock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class RejectOrderTransitionTest {

    private final ServiceOrderStatusHistoryGateway statusHistoryGateway = mock(ServiceOrderStatusHistoryGateway.class);
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final RejectOrderTransition transition = new RejectOrderTransition(statusHistoryGateway, registry);

    @Test
    void shouldRejectOrderWhenStatusIsWaitingApproval() {
        ServiceOrder order = ServiceOrderMock.waitingApprovalOrder();

        transition.execute(order, null);

        assertEquals(OrderStatusEnum.RECUSADO, order.getStatus());
        verify(statusHistoryGateway).save(any(ServiceOrderStatusHistory.class));
        assertEquals(1, registry.get("service_order.status.duration")
                .tag("from_status", "AGUARDANDO_APROVACAO")
                .tag("to_status", "RECUSADO")
                .timer().count());
    }

    @Test
    void shouldThrowExceptionWhenStatusIsNotWaitingApproval() {
        ServiceOrder order = ServiceOrderMock.inDiagnosisOrderWithBudget(BigDecimal.valueOf(100));

        InvalidOrderTransitionException exception = assertThrows(
                InvalidOrderTransitionException.class, () -> transition.execute(order, null));

        assertTrue(exception.getMessage().contains("Transição inválida"));
        assertEquals(OrderStatusEnum.EM_DIAGNOSTICO, order.getStatus());
        verifyNoInteractions(statusHistoryGateway);
    }
}
