package com.fiap.mechanical_hub.core_service.application.dto.serviceorder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceOrderCustomerView(
        String orderNumber,
        String customerName,
        String vehicleLicensePlate,
        String vehicleModel,
        String vehicleBrand,
        String status,
        BigDecimal budget,
        LocalDateTime openedAt,
        LocalDateTime completedAt,
        LocalDateTime deliveredAt
) {}
