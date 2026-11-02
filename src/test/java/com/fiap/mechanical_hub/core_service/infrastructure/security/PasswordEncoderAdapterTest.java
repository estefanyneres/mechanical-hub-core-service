package com.fiap.mechanical_hub.core_service.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordEncoderAdapterTest {

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private PasswordEncoderAdapter adapter;

    @Test
    void shouldDelegateEncodingToEncoder() {
        when(encoder.encode("raw123")).thenReturn("encoded123");

        assertEquals("encoded123", adapter.encode("raw123"));
    }
}
