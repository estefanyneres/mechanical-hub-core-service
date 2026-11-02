package com.fiap.mechanical_hub.core_service.infrastructure.security;

import com.fiap.mechanical_hub.core_service.domain.entities.Profile;
import com.fiap.mechanical_hub.core_service.domain.entities.User;
import com.fiap.mechanical_hub.core_service.domain.enums.ProfileEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private final TokenService tokenService = new TokenService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "secret", "test-secret-key-0123456789");
    }

    @Test
    void shouldGenerateAndValidateToken() {
        User user = User.create("João", "joao@x.com", "hash", Profile.create(ProfileEnum.MECHANICAL));

        String token = tokenService.generateToken(user);

        assertNotNull(token);
        assertEquals("joao@x.com", tokenService.validateToken(token));
    }

    @Test
    void shouldReturnEmptyStringForInvalidToken() {
        assertEquals("", tokenService.validateToken("token.invalido.aqui"));
    }

    @Test
    void shouldDecodeValidToken() {
        User user = User.create("João", "joao@x.com", "hash", Profile.create(ProfileEnum.MECHANICAL));
        String token = tokenService.generateToken(user);

        assertNotNull(tokenService.decode(token));
    }

    @Test
    void shouldEmbedProfileClaimAndRoundTrip() {
        User user = User.create("João", "joao@x.com", "hash", Profile.create(ProfileEnum.MECHANICAL));
        String token = tokenService.generateToken(user);

        var decoded = tokenService.decode(token);

        assertEquals("MECHANICAL", tokenService.getProfileClaim(decoded));
    }

    @Test
    void shouldEmbedAdministratorProfileClaim() {
        User user = User.create("Admin", "admin@x.com", "hash", Profile.create(ProfileEnum.ADMINISTRATOR));
        String token = tokenService.generateToken(user);

        var decoded = tokenService.decode(token);

        assertEquals("ADMINISTRATOR", tokenService.getProfileClaim(decoded));
    }

    @Test
    void shouldReturnNullWhenDecodingInvalidToken() {
        assertNull(tokenService.decode("invalido"));
    }

    @Test
    void shouldNotValidateTokenSignedWithDifferentSecret() {
        User user = User.create("João", "joao@x.com", "hash", Profile.create(ProfileEnum.MECHANICAL));
        String token = tokenService.generateToken(user);

        ReflectionTestUtils.setField(tokenService, "secret", "outro-secret-completamente-diferente");

        assertEquals("", tokenService.validateToken(token));
    }
}
