package com.fiap.mechanical_hub.core_service.application.dto.vehicle;

public record UpsertVehicleRequest(
        String licensePlate,
        String brand,
        String model,
        Integer year,
        String color
) {}
