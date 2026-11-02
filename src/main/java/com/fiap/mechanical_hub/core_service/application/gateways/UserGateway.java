package com.fiap.mechanical_hub.core_service.application.gateways;

import com.fiap.mechanical_hub.core_service.domain.entities.User;

import java.util.Optional;

public interface UserGateway {
    Optional<User> findByEmail(String email);
    User save(User user);
}
