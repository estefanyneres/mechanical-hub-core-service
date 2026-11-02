package com.fiap.mechanical_hub.core_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.core_service.application.gateways.UserGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.Profile;
import com.fiap.mechanical_hub.core_service.domain.entities.User;
import com.fiap.mechanical_hub.core_service.domain.enums.ProfileEnum;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.ProfileModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.UserModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.repositories.ProfileJpaRepository;
import com.fiap.mechanical_hub.core_service.infrastructure.database.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserGatewayImpl implements UserGateway {

    private final UserJpaRepository userJpaRepository;
    private final ProfileJpaRepository profileJpaRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        var model = userJpaRepository.findByEmail(email);
        return model != null ? Optional.of(toDomain(model)) : Optional.empty();
    }

    @Override
    public User save(User user) {
        ProfileModel profileModel = profileJpaRepository.findByName(user.getProfile().getName());
        return toDomain(userJpaRepository.save(toModel(user, profileModel)));
    }

    private User toDomain(UserModel model) {
        Profile profile = Profile.create(ProfileEnum.valueOf(model.getProfile().getName()));
        return User.build(model.getId(), model.getName(), model.getEmail(), model.getPasswordHash(), profile);
    }

    private UserModel toModel(User user, ProfileModel profileModel) {
        return new UserModel(user.getId(), user.getName(), user.getEmail(), user.getPasswordHash(), profileModel);
    }
}
