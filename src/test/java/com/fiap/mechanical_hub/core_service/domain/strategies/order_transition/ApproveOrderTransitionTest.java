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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ApproveOrderTransitionTest {

    private final ServiceOrderStatusHistoryGateway statusHistoryGateway = mock(ServiceOrderStatusHistoryGateway.class);
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final ApproveOrderTransition transition = new ApproveOrderTransition(statusHistoryGateway, registry);
    private final UUID userId = UUID.randomUUID();

    @Test
    void shouldApproveOrderWhenStatusIsWaitingApproval() {
        ServiceOrder order = ServiceOrderMock.waitingApprovalOrder();

        transition.execute(order, userId);

        assertEquals(OrderStatusEnum.APROVADO, order.getStatus());
        verify(statusHistoryGateway).save(any(ServiceOrderStatusHistory.class));
        assertEquals(1, registry.get("service_order.status.duration")
                .tag("from_status", "AGUARDANDO_APROVACAO")
                .tag("to_status", "APROVADO")
                .timer().count());
    }

    @Test
    void shouldThrowExceptionWhenApprovingFromNonWaitingApprovalStatus() {
        ServiceOrder order = ServiceOrderMock.inDiagnosisOrderWithBudget(BigDecimal.valueOf(100));

        InvalidOrderTransitionException exception = assertThrows(
                InvalidOrderTransitionException.class, () -> transition.execute(order, userId));

        assertTrue(exception.getMessage().contains("Transição inválida"));
        assertEquals(OrderStatusEnum.EM_DIAGNOSTICO, order.getStatus());
        verifyNoInteractions(statusHistoryGateway);
    }
}
