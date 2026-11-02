package com.fiap.mechanical_hub.core_service.application.dto.serviceorder;

public record VehicleData(
        String licensePlate,
        String brand,
        String model,
        Integer year,
        String color
) {}
