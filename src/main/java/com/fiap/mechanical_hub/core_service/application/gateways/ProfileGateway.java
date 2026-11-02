package com.fiap.mechanical_hub.core_service.application.gateways;

import com.fiap.mechanical_hub.core_service.domain.entities.Profile;

import java.util.Optional;

public interface ProfileGateway {
    Optional<Profile> findByName(String name);
}
