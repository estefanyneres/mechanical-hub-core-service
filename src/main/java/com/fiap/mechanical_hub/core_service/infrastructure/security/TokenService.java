package com.fiap.mechanical_hub.core_service.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fiap.mechanical_hub.core_service.application.gateways.TokenGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.User;
import com.fiap.mechanical_hub.core_service.domain.exceptions.TokenErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService implements TokenGateway {
    @Value("${api.security.jwt.secret}")
    private String secret;

    public String generateToken(User user){
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getEmail())
                    .withClaim("profile", user.getProfile().getName())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new TokenErrorException("Error while generating token", exception);
        }
    }

    public String validateToken(String token){
        DecodedJWT decoded = decode(token);
        return decoded == null ? "" : decoded.getSubject();
    }

    /**
     * Outros servicos (sem acesso ao banco de usuarios, exclusivo deste
     * servico - ADR-001) leem o perfil direto do claim do token, sem
     * chamada de rede: {@code decoded.getClaim("profile").asString()}.
     * Mantido aqui tambem como metodo de conveniencia pra quem decodifica
     * localmente.
     */
    public String getProfileClaim(DecodedJWT decoded) {
        return decoded == null ? null : decoded.getClaim("profile").asString();
    }

    public DecodedJWT decode(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token);
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    private Instant genExpirationDate(){
        return LocalDateTime.now().plusHours(1).toInstant(ZoneOffset.of("-03:00"));
    }
}
