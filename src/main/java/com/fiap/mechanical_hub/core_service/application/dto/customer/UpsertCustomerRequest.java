package com.fiap.mechanical_hub.core_service.application.dto.customer;

public record UpsertCustomerRequest(
        String name,
        String documentType,
        String documentNumber,
        String telephone,
        String email,
        String address
) {}
