package com.fiap.mechanical_hub.core_service.application.dto.serviceorder;

import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceOrderSummaryResponse(
        UUID id,
        String orderNumber,
        String status,
        String customerName,
        String vehicleInfo,
        LocalDateTime createdAt
) {}
