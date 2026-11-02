package com.fiap.mechanical_hub.core_service.infrastructure.security;

import com.fiap.mechanical_hub.core_service.application.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDetailsServiceAdapter implements UserDetailsService {

    private final UserGateway userGateway;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userGateway.findByEmail(email)
                .map(UserSecurityAdapter::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
