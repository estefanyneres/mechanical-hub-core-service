package com.fiap.mechanical_hub.core_service.application.gateways;

import com.fiap.mechanical_hub.core_service.domain.entities.User;

public interface TokenGateway {
    String generateToken(User user);
}
