package com.fiap.mechanical_hub.core_service.infrastructure.http.controllers;

import com.fiap.mechanical_hub.core_service.application.controllers.AuthenticationController;
import com.fiap.mechanical_hub.core_service.application.dto.authentication.AuthenticationRequest;
import com.fiap.mechanical_hub.core_service.application.dto.authentication.LoginResponse;
import com.fiap.mechanical_hub.core_service.application.dto.authentication.RegisterRequest;
import com.fiap.mechanical_hub.core_service.application.gateways.PasswordEncoderGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.ProfileGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.TokenGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.UserGateway;
import com.fiap.mechanical_hub.core_service.infrastructure.security.UserSecurityAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@Transactional
@RestController
@RequestMapping("/auth")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e registro de usuários (funcionários)")
public class AuthResource {

    private static final String NAME_PATTERN = "^[A-Za-zÀ-ÖØ-öø-ÿ ]+$";
    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final AuthenticationManager authenticationManager;
    private final AuthenticationController controller;

    public AuthResource(AuthenticationManager authenticationManager,
                        UserGateway userGateway, ProfileGateway profileGateway,
                        PasswordEncoderGateway passwordEncoderGateway, TokenGateway tokenGateway) {
        this.authenticationManager = authenticationManager;
        this.controller = new AuthenticationController(userGateway, profileGateway, passwordEncoderGateway, tokenGateway);
    }

    @PostMapping("/login")
    @Operation(summary = "Login de usuário",
            description = "Autentica um usuário com email e senha. Retorna um token JWT válido por 1 hora.",
            tags = {"Autenticação"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token gerado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<LoginResponse> login(@RequestBody AuthenticationRequest request) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(request.login(), request.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        UserSecurityAdapter userDetails = (UserSecurityAdapter) auth.getPrincipal();
        return ResponseEntity.ok(controller.login(userDetails.getUsername()));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário",
            description = "Cria um novo usuário no sistema. Requer perfil de Administrador.",
            tags = {"Autenticação"})
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou usuário já existe"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)")
    })
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            validate(request);
            controller.register(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    private void validate(RegisterRequest request) {
        if (request.name() == null || request.name().isBlank())
            throw new IllegalArgumentException("Nome é obrigatório");
        if (!request.name().matches(NAME_PATTERN))
            throw new IllegalArgumentException("Nome deve conter apenas letras e espaços");
        if (request.login() == null || request.login().isBlank())
            throw new IllegalArgumentException("Login é obrigatório");
        if (!request.login().matches(EMAIL_PATTERN))
            throw new IllegalArgumentException("Login deve ser um email válido");
        if (request.password() == null || request.password().isBlank())
            throw new IllegalArgumentException("Senha é obrigatória");
        if (request.password().length() < 6)
            throw new IllegalArgumentException("Senha deve ter no mínimo 6 caracteres");
        if (request.profile() == null || request.profile().isBlank())
            throw new IllegalArgumentException("Perfil é obrigatório");
        if (!request.profile().matches(NAME_PATTERN))
            throw new IllegalArgumentException("Perfil deve conter apenas letras e espaços");
    }
}
