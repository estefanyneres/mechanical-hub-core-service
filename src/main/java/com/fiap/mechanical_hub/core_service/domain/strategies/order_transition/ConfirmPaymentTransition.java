package com.fiap.mechanical_hub.core_service.domain.strategies.order_transition;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.UUID;

/**
 * Disparada pelo SagaOrchestratorUseCase ao receber pagamento.confirmado.
 * Valido tanto a partir de APROVADO (primeira tentativa) quanto de
 * FALHA_PAGAMENTO (retentativa) - ver ServiceOrder.confirmPayment().
 */
public class ConfirmPaymentTransition implements OrderStatusTransition {

    private final ServiceOrderStatusHistoryGateway statusHistoryGateway;
    private final MeterRegistry registry;

    public ConfirmPaymentTransition(ServiceOrderStatusHistoryGateway statusHistoryGateway, MeterRegistry registry) {
        this.statusHistoryGateway = statusHistoryGateway;
        this.registry = registry;
    }

    @Override
    public void execute(ServiceOrder order, UUID userId) {
        OrderStatusEnum fromStatus = order.getStatus();
        order.confirmPayment();
        ServiceOrderStatusTransitionRecorder.record(
                statusHistoryGateway, registry, order, fromStatus, order.getStatus(), userId);
    }
}
