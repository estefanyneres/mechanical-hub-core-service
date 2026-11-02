package com.fiap.mechanical_hub.core_service.application.gateways;

public interface PasswordEncoderGateway {
    String encode(String rawPassword);
}
