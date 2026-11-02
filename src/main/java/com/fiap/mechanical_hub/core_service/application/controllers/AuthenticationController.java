package com.fiap.mechanical_hub.core_service.application.controllers;

import com.fiap.mechanical_hub.core_service.application.dto.authentication.LoginResponse;
import com.fiap.mechanical_hub.core_service.application.dto.authentication.RegisterRequest;
import com.fiap.mechanical_hub.core_service.application.gateways.PasswordEncoderGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.ProfileGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.TokenGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.UserGateway;
import com.fiap.mechanical_hub.core_service.application.usecases.AuthorizationUseCase;

public class AuthenticationController {

    private final AuthorizationUseCase useCase;

    public AuthenticationController(UserGateway userGateway, ProfileGateway profileGateway,
                                    PasswordEncoderGateway passwordEncoderGateway, TokenGateway tokenGateway) {
        this.useCase = new AuthorizationUseCase(userGateway, profileGateway, passwordEncoderGateway, tokenGateway);
    }

    public LoginResponse login(String username) {
        return useCase.login(username);
    }

    public void register(RegisterRequest request) {
        useCase.registerNewUser(request);
    }
}
