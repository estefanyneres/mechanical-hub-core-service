package com.fiap.mechanical_hub.core_service.infrastructure.security;

import com.fiap.mechanical_hub.core_service.application.gateways.UserGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.Profile;
import com.fiap.mechanical_hub.core_service.domain.entities.User;
import com.fiap.mechanical_hub.core_service.domain.enums.ProfileEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceAdapterTest {

    @Mock
    private UserGateway userGateway;

    @InjectMocks
    private UserDetailsServiceAdapter adapter;

    @Test
    void shouldLoadUserByUsername() {
        User user = User.create("João", "joao@x.com", "hash", Profile.create(ProfileEnum.MECHANICAL));
        when(userGateway.findByEmail("joao@x.com")).thenReturn(Optional.of(user));

        UserDetails result = adapter.loadUserByUsername("joao@x.com");

        assertEquals("joao@x.com", result.getUsername());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userGateway.findByEmail("naoexiste@x.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> adapter.loadUserByUsername("naoexiste@x.com"));
    }
}
