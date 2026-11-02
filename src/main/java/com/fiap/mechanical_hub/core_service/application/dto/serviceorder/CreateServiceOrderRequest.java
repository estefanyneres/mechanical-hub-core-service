package com.fiap.mechanical_hub.core_service.application.dto.serviceorder;

public record CreateServiceOrderRequest(
        CustomerData customer,
        VehicleData vehicle,
        String requestDescription
) {}
