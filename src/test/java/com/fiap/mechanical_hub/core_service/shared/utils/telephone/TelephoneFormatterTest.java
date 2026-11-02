package com.fiap.mechanical_hub.core_service.shared.utils.telephone;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TelephoneFormatterTest {

    @Test
    void shouldReturnNullWhenInputIsNull() {
        assertNull(TelephoneFormatter.formatTelephone(null));
    }

    @Test
    void shouldFormatLandline10Digits() {
        assertEquals("(11) 2345-6789", TelephoneFormatter.formatTelephone("1123456789"));
    }

    @Test
    void shouldFormatMobile11Digits() {
        assertEquals("(11) 9 2345-6789", TelephoneFormatter.formatTelephone("11923456789"));
    }

    @Test
    void shouldFormatWithCountryCode12Digits() {
        assertEquals("+55 (11) 2345-6789", TelephoneFormatter.formatTelephone("551123456789"));
    }

    @Test
    void shouldFormatWithCountryCode13Digits() {
        assertEquals("+55 (11) 9 2345-6789", TelephoneFormatter.formatTelephone("5511923456789"));
    }

    @Test
    void shouldHandleAlreadyFormattedInputs() {
        assertEquals("(11) 2345-6789", TelephoneFormatter.formatTelephone("(11)2345-6789"));
        assertEquals("(11) 9 2345-6789", TelephoneFormatter.formatTelephone("(11) 9 2345-6789"));
        assertEquals("+55 (11) 9 2345-6789", TelephoneFormatter.formatTelephone("+55 (11) 9 2345-6789"));
    }

    @Test
    void shouldThrowWhenInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> TelephoneFormatter.formatTelephone("123"));
        assertThrows(IllegalArgumentException.class, () -> TelephoneFormatter.formatTelephone("12345678901234"));
    }
}
