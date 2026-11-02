package com.fiap.mechanical_hub.core_service.domain.strategies.order_transition;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.UUID;

/**
 * Disparada pelo SagaOrchestratorUseCase ao receber o evento
 * execucao.finalizada (TASK-016), nao mais por uma checagem local de
 * tarefas -- a entidade ServiceOrder nao conhece mais OrderTask.
 */
public class FinishOrderTransition implements OrderStatusTransition {

    private final ServiceOrderStatusHistoryGateway statusHistoryGateway;
    private final MeterRegistry registry;

    public FinishOrderTransition(ServiceOrderStatusHistoryGateway statusHistoryGateway, MeterRegistry registry) {
        this.statusHistoryGateway = statusHistoryGateway;
        this.registry = registry;
    }

    @Override
    public void execute(ServiceOrder order, UUID userId) {
        OrderStatusEnum fromStatus = order.getStatus();
        order.finish();
        ServiceOrderStatusTransitionRecorder.record(
                statusHistoryGateway, registry, order, fromStatus, order.getStatus(), userId);
    }
}
