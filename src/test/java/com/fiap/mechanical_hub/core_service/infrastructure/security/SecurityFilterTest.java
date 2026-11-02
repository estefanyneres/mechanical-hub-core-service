package com.fiap.mechanical_hub.core_service.infrastructure.security;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fiap.mechanical_hub.core_service.application.gateways.CustomerGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.UserGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.Profile;
import com.fiap.mechanical_hub.core_service.domain.entities.User;
import com.fiap.mechanical_hub.core_service.domain.entities.mocks.CustomerMock;
import com.fiap.mechanical_hub.core_service.domain.enums.ProfileEnum;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock private TokenService tokenService;
    @Mock private UserGateway userGateway;
    @Mock private CustomerGateway customerGateway;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private SecurityFilter securityFilter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateEmployeeWhenTokenHasNoCustomerType() throws Exception {
        User user = User.create("João", "joao@x.com", "hash", Profile.create(ProfileEnum.MECHANICAL));
        DecodedJWT decoded = mock(DecodedJWT.class);
        Claim typeClaim = mock(Claim.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(tokenService.decode("token123")).thenReturn(decoded);
        when(decoded.getClaim("type")).thenReturn(typeClaim);
        when(typeClaim.asString()).thenReturn(null);
        when(decoded.getSubject()).thenReturn("joao@x.com");
        when(userGateway.findByEmail("joao@x.com")).thenReturn(Optional.of(user));

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldAuthenticateCustomerWhenTokenHasCustomerType() throws Exception {
        var customer = CustomerMock.defaultCustomer();
        DecodedJWT decoded = mock(DecodedJWT.class);
        Claim typeClaim = mock(Claim.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer token456");
        when(tokenService.decode("token456")).thenReturn(decoded);
        when(decoded.getClaim("type")).thenReturn(typeClaim);
        when(typeClaim.asString()).thenReturn("customer");
        when(decoded.getSubject()).thenReturn(customer.getId().toString());
        when(customerGateway.findById(customer.getId())).thenReturn(Optional.of(customer));

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotAuthenticateWhenNoAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsInvalid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid");
        when(tokenService.decode("invalid")).thenReturn(null);

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotAuthenticateCustomerWhenSubjectIsNotValidUuid() throws Exception {
        DecodedJWT decoded = mock(DecodedJWT.class);
        Claim typeClaim = mock(Claim.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer token789");
        when(tokenService.decode("token789")).thenReturn(decoded);
        when(decoded.getClaim("type")).thenReturn(typeClaim);
        when(typeClaim.asString()).thenReturn("customer");
        when(decoded.getSubject()).thenReturn("not-a-uuid");

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(customerGateway);
    }
}
