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

public class AuthorizationUseCase {

    private final UserGateway userGateway;
    private final ProfileGateway profileGateway;
    private final PasswordEncoderGateway passwordEncoder;
    private final TokenGateway tokenGateway;

    public AuthorizationUseCase(UserGateway userGateway, ProfileGateway profileGateway,
                                 PasswordEncoderGateway passwordEncoder, TokenGateway tokenGateway) {
        this.userGateway = userGateway;
        this.profileGateway = profileGateway;
        this.passwordEncoder = passwordEncoder;
        this.tokenGateway = tokenGateway;
    }

    public LoginResponse login(String userLogin) {
        User user = userGateway.findByEmail(userLogin)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado: " + userLogin));
        String token = tokenGateway.generateToken(user);
        return new LoginResponse(token);
    }

    public void registerNewUser(RegisterRequest request) {
        if (userGateway.findByEmail(request.login()).isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail");
        }

        profileGateway.findByName(request.profile())
                .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado: " + request.profile()));

        String encryptedPassword = passwordEncoder.encode(request.password());
        User newUser = User.create(
                request.name(),
                request.login(),
                encryptedPassword,
                Profile.create(ProfileEnum.valueOf(request.profile()))
        );

        userGateway.save(newUser);
    }
}
