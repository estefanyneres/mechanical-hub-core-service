package com.fiap.mechanical_hub.core_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.core_service.domain.entities.Profile;
import com.fiap.mechanical_hub.core_service.domain.entities.User;
import com.fiap.mechanical_hub.core_service.domain.enums.ProfileEnum;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.ProfileModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.UserModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.repositories.ProfileJpaRepository;
import com.fiap.mechanical_hub.core_service.infrastructure.database.repositories.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserGatewayImplTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private ProfileJpaRepository profileJpaRepository;

    @InjectMocks
    private UserGatewayImpl gateway;

    @Test
    void shouldFindByEmail() {
        ProfileModel profileModel = new ProfileModel(UUID.randomUUID(), "ADMINISTRATOR", null);
        UserModel userModel = new UserModel(UUID.randomUUID(), "João", "joao@x.com", "hash", profileModel);
        when(userJpaRepository.findByEmail("joao@x.com")).thenReturn(userModel);

        Optional<User> result = gateway.findByEmail("joao@x.com");

        assertTrue(result.isPresent());
        assertEquals("João", result.get().getName());
        assertEquals(ProfileEnum.ADMINISTRATOR.name(), result.get().getProfile().getName());
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        when(userJpaRepository.findByEmail("naoexiste@x.com")).thenReturn(null);

        assertTrue(gateway.findByEmail("naoexiste@x.com").isEmpty());
    }

    @Test
    void shouldSaveUser() {
        User user = User.create("João", "joao@x.com", "hash", Profile.create(ProfileEnum.MECHANICAL));
        ProfileModel profileModel = new ProfileModel(UUID.randomUUID(), "MECHANICAL", null);
        UserModel savedModel = new UserModel(user.getId(), user.getName(), user.getEmail(), user.getPasswordHash(), profileModel);

        when(profileJpaRepository.findByName("MECHANICAL")).thenReturn(profileModel);
        when(userJpaRepository.save(any(UserModel.class))).thenReturn(savedModel);

        User result = gateway.save(user);

        assertEquals(user.getEmail(), result.getEmail());
    }
}
