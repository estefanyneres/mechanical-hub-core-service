package com.fiap.mechanical_hub.core_service.domain.strategies.order_transition;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.UUID;

public class RejectOrderTransition implements OrderStatusTransition {

    private final ServiceOrderStatusHistoryGateway statusHistoryGateway;
    private final MeterRegistry registry;

    public RejectOrderTransition(ServiceOrderStatusHistoryGateway statusHistoryGateway, MeterRegistry registry) {
        this.statusHistoryGateway = statusHistoryGateway;
        this.registry = registry;
    }

    @Override
    public void execute(ServiceOrder order, UUID userId) {
        OrderStatusEnum fromStatus = order.getStatus();
        order.reject();
        ServiceOrderStatusTransitionRecorder.record(
                statusHistoryGateway, registry, order, fromStatus, order.getStatus(), userId);
    }
}
