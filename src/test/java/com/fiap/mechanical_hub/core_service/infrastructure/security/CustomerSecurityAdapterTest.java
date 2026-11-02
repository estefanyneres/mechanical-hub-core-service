package com.fiap.mechanical_hub.core_service.infrastructure.security;

import com.fiap.mechanical_hub.core_service.domain.entities.mocks.CustomerMock;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerSecurityAdapterTest {

    @Test
    void shouldExposeUserDetailsFromCustomer() {
        Customer customer = CustomerMock.defaultCustomer();
        CustomerSecurityAdapter adapter = new CustomerSecurityAdapter(customer);

        assertEquals(customer.getId().toString(), adapter.getUsername());
        assertEquals("", adapter.getPassword());
        assertTrue(adapter.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
        assertTrue(adapter.isAccountNonExpired());
        assertTrue(adapter.isAccountNonLocked());
        assertTrue(adapter.isCredentialsNonExpired());
        assertTrue(adapter.isEnabled());
        assertEquals(customer, adapter.customer());
    }
}
