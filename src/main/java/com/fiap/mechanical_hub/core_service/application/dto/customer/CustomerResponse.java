package com.fiap.mechanical_hub.core_service.application.dto.customer;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String documentType,
        String documentNumber,
        String telephone,
        String email,
        String address,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
