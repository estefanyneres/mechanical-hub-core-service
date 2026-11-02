package com.fiap.mechanical_hub.core_service.application.dto.serviceorder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceOrderResponse(
        UUID id,
        String orderNumber,
        String status,
        UUID vehicleId,
        UUID customerId,
        UUID createdByUserId,
        UUID responsibleUserId,
        String requestDescription,
        BigDecimal budget,
        LocalDateTime estimatedCompletionAt,
        LocalDateTime openedAt,
        LocalDateTime completedAt,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
