package com.fiap.mechanical_hub.core_service.domain.strategies.order_transition;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.UUID;

/**
 * Disparada pelo SagaOrchestratorUseCase ao receber pagamento.falhou.
 */
public class FailPaymentTransition implements OrderStatusTransition {

    private final ServiceOrderStatusHistoryGateway statusHistoryGateway;
    private final MeterRegistry registry;

    public FailPaymentTransition(ServiceOrderStatusHistoryGateway statusHistoryGateway, MeterRegistry registry) {
        this.statusHistoryGateway = statusHistoryGateway;
        this.registry = registry;
    }

    @Override
    public void execute(ServiceOrder order, UUID userId) {
        OrderStatusEnum fromStatus = order.getStatus();
        order.failPayment();
        ServiceOrderStatusTransitionRecorder.record(
                statusHistoryGateway, registry, order, fromStatus, order.getStatus(), userId);
    }
}
