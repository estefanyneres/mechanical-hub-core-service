package com.fiap.mechanical_hub.core_service.infrastructure.security;

import com.fiap.mechanical_hub.core_service.domain.entities.Profile;
import com.fiap.mechanical_hub.core_service.domain.entities.User;
import com.fiap.mechanical_hub.core_service.domain.enums.ProfileEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserSecurityAdapterTest {

    @Test
    void shouldExposeUserDetailsFromUser() {
        User user = User.create("João", "joao@x.com", "hashedPassword", Profile.create(ProfileEnum.ADMINISTRATOR));
        UserSecurityAdapter adapter = new UserSecurityAdapter(user);

        assertEquals("joao@x.com", adapter.getUsername());
        assertEquals("hashedPassword", adapter.getPassword());
        assertTrue(adapter.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR")));
        assertTrue(adapter.isAccountNonExpired());
        assertTrue(adapter.isAccountNonLocked());
        assertTrue(adapter.isCredentialsNonExpired());
        assertTrue(adapter.isEnabled());
        assertEquals(user, adapter.user());
    }
}
