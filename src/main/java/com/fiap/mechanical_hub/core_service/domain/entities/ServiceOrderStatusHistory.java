package com.fiap.mechanical_hub.core_service.domain.entities;

import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public class ServiceOrderStatusHistory {

    private UUID id;
    private UUID serviceOrderId;
    private OrderStatusEnum fromStatus;
    private OrderStatusEnum toStatus;
    private UUID changedByUserId;
    private LocalDateTime changedAt;

    public ServiceOrderStatusHistory() {
    }

    public ServiceOrderStatusHistory(
            UUID id,
            UUID serviceOrderId,
            OrderStatusEnum fromStatus,
            OrderStatusEnum toStatus,
            UUID changedByUserId,
            LocalDateTime changedAt) {
        this.id = id;
        this.serviceOrderId = serviceOrderId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedByUserId = changedByUserId;
        this.changedAt = changedAt;
    }

    public static ServiceOrderStatusHistory register(
            UUID serviceOrderId, OrderStatusEnum fromStatus, OrderStatusEnum toStatus, UUID changedByUserId) {
        return new ServiceOrderStatusHistory(
                UUID.randomUUID(),
                serviceOrderId,
                fromStatus,
                toStatus,
                changedByUserId,
                LocalDateTime.now()
        );
    }

    public UUID getId() { return id; }
    public UUID getServiceOrderId() { return serviceOrderId; }
    public OrderStatusEnum getFromStatus() { return fromStatus; }
    public OrderStatusEnum getToStatus() { return toStatus; }
    public UUID getChangedByUserId() { return changedByUserId; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
