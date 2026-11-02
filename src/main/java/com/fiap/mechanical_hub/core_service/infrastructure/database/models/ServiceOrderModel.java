package com.fiap.mechanical_hub.core_service.infrastructure.database.models;

import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Sem os_tasks (repair-service) e sem has_stock_pending (TASK-015) --
 * cabecalho + status, exatamente o escopo definido na ADR-001.
 */
@Entity
@Table(name = "service_orders", indexes = {
        @Index(name = "idx_service_orders_vehicle_id", columnList = "vehicle_id"),
        @Index(name = "idx_service_orders_customer_id", columnList = "customer_id"),
        @Index(name = "idx_service_orders_order_status", columnList = "order_status"),
        @Index(name = "idx_service_orders_created_by_user_id", columnList = "created_by_user_id"),
        @Index(name = "idx_service_orders_responsible_user_id", columnList = "responsible_user_id"),
        @Index(name = "idx_service_orders_order_number", columnList = "order_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ServiceOrderModel {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatusEnum orderStatusEnum;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(name = "responsible_user_id")
    private UUID responsibleUserId;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "request_description", nullable = false, length = 255)
    private String requestDescription;

    @Column(name = "budget", precision = 12, scale = 2)
    private BigDecimal budget;

    @Column(name = "estimated_completion_at")
    private LocalDateTime estimatedCompletionAt;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
