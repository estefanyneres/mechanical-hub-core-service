package com.fiap.mechanical_hub.core_service.infrastructure.database.models;

import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_order_status_history")
@Getter
@Setter
public class ServiceOrderStatusHistoryModel {

    @Id
    private UUID id;

    @Column(name = "service_order_id", nullable = false)
    private UUID serviceOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", nullable = false)
    private OrderStatusEnum fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private OrderStatusEnum toStatus;

    @Column(name = "changed_by_user_id", nullable = true)
    private UUID changedByUserId;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public ServiceOrderStatusHistoryModel() {
    }

    public ServiceOrderStatusHistoryModel(
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
}
