package com.fiap.mechanical_hub.core_service.application.usecases;

import com.fiap.mechanical_hub.core_service.application.dto.authentication.LoginResponse;
import com.fiap.mechanical_hub.core_service.application.dto.authentication.RegisterRequest;
import com.fiap.mechanical_hub.core_service.application.gateways.PasswordEncoderGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.ProfileGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.TokenGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.UserGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.Profile;
import com.fiap.mechanical_hub.core_service.domain.entities.User;
import com.fiap.mechanical_hub.core_service.domain.enums.ProfileEnum;
import com.fiap.mechanical_hub.core_service.domain.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationUseCaseTest {

    @Mock
    private UserGateway userGateway;

    @Mock
    private ProfileGateway profileGateway;

    @Mock
    private PasswordEncoderGateway passwordEncoder;

    @Mock
    private TokenGateway tokenGateway;

    @InjectMocks
    private AuthorizationUseCase authorizationUseCase;

    private RegisterRequest registerRequest;
    private User user;
    private Profile profile;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("João Silva", "user@example.com", "password123", "MECHANICAL");
        user = User.create("João Silva", "user@example.com", "hashedPassword", Profile.create(ProfileEnum.MECHANICAL));
        profile = Profile.create(ProfileEnum.MECHANICAL);
    }

    @Test
    void shouldRegisterNewUserSuccessfully() {
        when(userGateway.findByEmail(registerRequest.login())).thenReturn(Optional.empty());
        when(profileGateway.findByName(registerRequest.profile())).thenReturn(Optional.of(profile));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        assertDoesNotThrow(() -> authorizationUseCase.registerNewUser(registerRequest));

        verify(userGateway).save(any(User.class));
    }

    @Test
    void shouldThrowIllegalArgumentWhenUserAlreadyExists() {
        when(userGateway.findByEmail(registerRequest.login())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> authorizationUseCase.registerNewUser(registerRequest));
        verify(userGateway, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentWhenProfileNotFound() {
        when(userGateway.findByEmail(registerRequest.login())).thenReturn(Optional.empty());
        when(profileGateway.findByName(registerRequest.profile())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authorizationUseCase.registerNewUser(registerRequest));
        verify(userGateway, never()).save(any());
    }

    @Test
    void shouldLoginAndReturnToken() {
        when(userGateway.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenGateway.generateToken(user)).thenReturn("jwt-token");

        LoginResponse result = authorizationUseCase.login(user.getEmail());

        assertEquals("jwt-token", result.token());
    }

    @Test
    void shouldThrowNotFoundWhenUserNotFoundOnLogin() {
        when(userGateway.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> authorizationUseCase.login("naoexiste@example.com"));
    }
}
