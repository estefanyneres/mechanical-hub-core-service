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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Le os tres tipos de JWT que chegam neste servico: o de funcionario
 * (emitido pelo /auth/login daqui mesmo), o de cliente (emitido pelo
 * auth-lambda via CPF, Fase 3) e o de servico (ServiceTokenIssuer do
 * billing-service, mesmo padrao ja usado entre repair-service/
 * stock-service -- "sub" e o nome do servico, nao um e-mail, por isso o
 * fallback quando nao acha usuario pelo e-mail). Todos usam o mesmo
 * JWT_SECRET.
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
                } else if (!authenticateUser(decoded.getSubject())) {
                    authenticateServiceToken(decoded);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean authenticateUser(String email) {
        return userGateway.findByEmail(email)
                .map(user -> {
                    setAuthentication(new UserSecurityAdapter(user));
                    return true;
                })
                .orElse(false);
    }

    private void authenticateServiceToken(DecodedJWT decoded) {
        String profile = decoded.getClaim("profile").asString();
        if (profile != null) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    decoded.getSubject(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + profile.toUpperCase())));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
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
