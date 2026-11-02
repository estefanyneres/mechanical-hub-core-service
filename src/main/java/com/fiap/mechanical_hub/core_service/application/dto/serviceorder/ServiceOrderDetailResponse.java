package com.fiap.mechanical_hub.core_service.application.dto.serviceorder;

import com.fiap.mechanical_hub.core_service.application.dto.customer.CustomerResponse;
import com.fiap.mechanical_hub.core_service.application.dto.vehicle.VehicleResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceOrderDetailResponse(
        UUID id,
        String orderNumber,
        String status,
        CustomerResponse customer,
        VehicleResponse vehicle,
        String requestDescription,
        BigDecimal budget,
        LocalDateTime createdAt
) {}
