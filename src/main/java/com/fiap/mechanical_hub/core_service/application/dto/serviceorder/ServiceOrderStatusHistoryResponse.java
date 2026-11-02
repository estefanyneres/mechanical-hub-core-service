package com.fiap.mechanical_hub.core_service.application.dto.serviceorder;

import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceOrderStatusHistoryResponse(
        String fromStatus,
        String toStatus,
        UUID changedByUserId,
        LocalDateTime changedAt
) {}
