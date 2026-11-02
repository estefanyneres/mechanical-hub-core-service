package com.fiap.mechanical_hub.core_service.infrastructure.security;

import com.fiap.mechanical_hub.core_service.application.gateways.PasswordEncoderGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordEncoderAdapter implements PasswordEncoderGateway {

    private final org.springframework.security.crypto.password.PasswordEncoder encoder;

    @Override
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
}
