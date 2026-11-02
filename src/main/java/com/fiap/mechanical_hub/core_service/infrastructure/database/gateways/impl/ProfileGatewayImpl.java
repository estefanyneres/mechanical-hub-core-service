package com.fiap.mechanical_hub.core_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.core_service.application.gateways.ProfileGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.Profile;
import com.fiap.mechanical_hub.core_service.domain.enums.ProfileEnum;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.ProfileModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.repositories.ProfileJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProfileGatewayImpl implements ProfileGateway {

    private final ProfileJpaRepository jpaRepository;

    @Override
    public Optional<Profile> findByName(String name) {
        ProfileModel model = jpaRepository.findByName(name);
        if (model == null) return Optional.empty();
        return Optional.of(Profile.create(ProfileEnum.valueOf(model.getName())));
    }
}
