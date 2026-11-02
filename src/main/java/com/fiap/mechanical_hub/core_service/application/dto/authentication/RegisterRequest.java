package com.fiap.mechanical_hub.core_service.application.dto.authentication;

public record RegisterRequest(
        String name,
        String login,
        String password,
        String profile
) {}
