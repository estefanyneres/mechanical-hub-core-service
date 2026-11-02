package com.fiap.mechanical_hub.core_service.application.controllers;

import com.fiap.mechanical_hub.core_service.application.dto.authentication.LoginResponse;
import com.fiap.mechanical_hub.core_service.application.dto.authentication.RegisterRequest;
import com.fiap.mechanical_hub.core_service.application.gateways.PasswordEncoderGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.ProfileGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.TokenGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.UserGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.Profile;
import com.fiap.mechanical_hub.core_service.domain.enums.ProfileEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock private UserGateway userGateway;
    @Mock private ProfileGateway profileGateway;
    @Mock private PasswordEncoderGateway passwordEncoderGateway;
    @Mock private TokenGateway tokenGateway;

    private AuthenticationController controller;

    @Test
    void shouldDelegateLoginToUseCase() {
        controller = new AuthenticationController(userGateway, profileGateway, passwordEncoderGateway, tokenGateway);
        var user = com.fiap.mechanical_hub.core_service.domain.entities.User.create(
                "João", "joao@x.com", "hash", Profile.create(ProfileEnum.ADMINISTRATOR));
        when(userGateway.findByEmail("joao@x.com")).thenReturn(Optional.of(user));
        when(tokenGateway.generateToken(user)).thenReturn("jwt");

        LoginResponse result = controller.login("joao@x.com");

        assertEquals("jwt", result.token());
    }

    @Test
    void shouldDelegateRegisterToUseCase() {
        controller = new AuthenticationController(userGateway, profileGateway, passwordEncoderGateway, tokenGateway);
        RegisterRequest request = new RegisterRequest("João", "joao@x.com", "senha123", "MECHANICAL");
        when(userGateway.findByEmail("joao@x.com")).thenReturn(Optional.empty());
        when(profileGateway.findByName("MECHANICAL")).thenReturn(Optional.of(Profile.create(ProfileEnum.MECHANICAL)));
        when(passwordEncoderGateway.encode(anyString())).thenReturn("hashed");

        controller.register(request);

        verify(userGateway).save(org.mockito.ArgumentMatchers.any());
    }
}
