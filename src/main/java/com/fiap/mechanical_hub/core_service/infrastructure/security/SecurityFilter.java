package com.fiap.mechanical_hub.core_service.infrastructure.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fiap.mechanical_hub.core_service.application.gateways.CustomerGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.UserGateway;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Le os dois tipos de JWT que chegam neste servico: o de funcionario
 * (emitido pelo /auth/login daqui mesmo) e o de cliente (emitido pelo
 * auth-lambda via CPF, Fase 3) -- os dois usam o mesmo JWT_SECRET.
 */
@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private static final String CUSTOMER_TOKEN_TYPE = "customer";

    private final TokenService tokenService;
    private final UserGateway userGateway;
    private final CustomerGateway customerGateway;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var token = this.recoverToken(request);

        if (token != null) {
            DecodedJWT decoded = tokenService.decode(token);

            if (decoded != null) {
                if (CUSTOMER_TOKEN_TYPE.equals(decoded.getClaim("type").asString())) {
                    authenticateCustomer(decoded.getSubject());
                } else {
                    authenticateUser(decoded.getSubject());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String email) {
        userGateway.findByEmail(email).ifPresent(user ->
                setAuthentication(new UserSecurityAdapter(user)));
    }

    private void authenticateCustomer(String subject) {
        try {
            UUID customerId = UUID.fromString(subject);
            customerGateway.findById(customerId).ifPresent(customer ->
                    setAuthentication(new CustomerSecurityAdapter(customer)));
        } catch (IllegalArgumentException ignored) {
            // subject nao e um UUID valido -- token malformado, segue sem autenticar
        }
    }

    private void setAuthentication(UserDetails userDetails) {
        var authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}
