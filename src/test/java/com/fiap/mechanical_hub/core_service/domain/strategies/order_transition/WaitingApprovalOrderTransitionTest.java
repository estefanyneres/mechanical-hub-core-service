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

class WaitingApprovalOrderTransitionTest {

    private final ServiceOrderStatusHistoryGateway statusHistoryGateway = mock(ServiceOrderStatusHistoryGateway.class);
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final WaitingApprovalOrderTransition transition =
            new WaitingApprovalOrderTransition(statusHistoryGateway, registry);
    private final UUID userId = UUID.randomUUID();

    @Test
    void shouldSubmitForApprovalWhenBudgetIsGenerated() {
        ServiceOrder order = ServiceOrderMock.inDiagnosisOrderWithBudget(BigDecimal.valueOf(500));

        transition.execute(order, userId);

        assertEquals(OrderStatusEnum.AGUARDANDO_APROVACAO, order.getStatus());
        verify(statusHistoryGateway).save(any(ServiceOrderStatusHistory.class));
        assertEquals(1, registry.get("service_order.status.duration")
                .tag("from_status", "EM_DIAGNOSTICO")
                .tag("to_status", "AGUARDANDO_APROVACAO")
                .timer().count());
    }

    /**
     * TASK-039: antes desse teste exigia BusinessRuleException sem
     * orcamento -- isso era do monolito. Na Fase 4 e o oposto: e exatamente
     * o pedido de orcamento que acontece DEPOIS dessa transicao
     * (ServiceOrderUseCase.updateOrderStatus -> SagaOrchestratorUseCase.
     * requestBudget), entao o orcamento tem que estar ausente aqui pra
     * refletir o fluxo real.
     */
    @Test
    void shouldSubmitForApprovalWhenBudgetIsNotGeneratedYet() {
        ServiceOrder order = ServiceOrderMock.inDiagnosisOrderWithoutBudget();

        transition.execute(order, userId);

        assertEquals(OrderStatusEnum.AGUARDANDO_APROVACAO, order.getStatus());
        verify(statusHistoryGateway).save(any(ServiceOrderStatusHistory.class));
    }

    @Test
    void shouldThrowInvalidOrderTransitionExceptionWhenStatusIsNotInDiagnosis() {
        ServiceOrder order = ServiceOrderMock.receivedOrder();

        InvalidOrderTransitionException exception = assertThrows(
                InvalidOrderTransitionException.class, () -> transition.execute(order, userId));

        assertTrue(exception.getMessage().contains("Transição inválida"));
        verifyNoInteractions(statusHistoryGateway);
    }
}
