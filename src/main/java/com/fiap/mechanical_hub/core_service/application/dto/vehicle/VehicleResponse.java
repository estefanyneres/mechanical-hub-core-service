package com.fiap.mechanical_hub.core_service.application.dto.vehicle;

import java.time.LocalDateTime;
import java.util.UUID;

public record VehicleResponse(
        UUID id,
        UUID customerId,
        String licensePlate,
        String brand,
        String model,
        Integer year,
        String color,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
