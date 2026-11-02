package com.fiap.mechanical_hub.core_service.application.dto.serviceorder;

public record CustomerData(
        String name,
        String documentType,
        String documentNumber,
        String telephone,
        String email,
        String address
) {}
