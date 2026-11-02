package com.fiap.mechanical_hub.core_service.domain.strategies.order_transition;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrderStatusHistory;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import com.fiap.mechanical_hub.core_service.infrastructure.metrics.ServiceOrderStatusMetrics;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public final class ServiceOrderStatusTransitionRecorder {

    private ServiceOrderStatusTransitionRecorder() {}

    public static void record(
            ServiceOrderStatusHistoryGateway statusHistoryGateway,
            MeterRegistry registry,
            ServiceOrder order,
            OrderStatusEnum fromStatus,
            OrderStatusEnum toStatus,
            UUID changedByUserId) {

        LocalDateTime lastChangedAt = statusHistoryGateway.findLastByServiceOrderId(order.getId())
                .map(ServiceOrderStatusHistory::getChangedAt)
                .orElseGet(order::getCreatedAt);

        ServiceOrderStatusMetrics.recordDuration(
                registry, fromStatus, toStatus, Duration.between(lastChangedAt, LocalDateTime.now()));

        statusHistoryGateway.save(
                ServiceOrderStatusHistory.register(order.getId(), fromStatus, toStatus, changedByUserId));
    }
}
