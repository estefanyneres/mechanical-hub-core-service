package com.fiap.mechanical_hub.core_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.core_service.domain.entities.Profile;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.ProfileModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.repositories.ProfileJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileGatewayImplTest {

    @Mock
    private ProfileJpaRepository jpaRepository;

    @InjectMocks
    private ProfileGatewayImpl gateway;

    @Test
    void shouldFindByName() {
        ProfileModel model = new ProfileModel(UUID.randomUUID(), "ADMINISTRATOR", null);
        when(jpaRepository.findByName("ADMINISTRATOR")).thenReturn(model);

        Optional<Profile> result = gateway.findByName("ADMINISTRATOR");

        assertTrue(result.isPresent());
        assertEquals("ADMINISTRATOR", result.get().getName());
    }

    @Test
    void shouldReturnEmptyWhenProfileNotFound() {
        when(jpaRepository.findByName("INEXISTENTE")).thenReturn(null);

        assertTrue(gateway.findByName("INEXISTENTE").isEmpty());
    }
}
