package com.fiap.mechanical_hub.core_service.shared.utils.license_plate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LicensePlateFormatterTest {

    @Test
    void shouldNormalize() {
        assertThat(LicensePlateFormatter.normalize("abc-1d23")).isEqualTo("ABC1D23");
        assertThat(LicensePlateFormatter.normalize("ABC-1234")).isEqualTo("ABC1234");
    }

    @Test
    void shouldReturnNullForNullOrEmpty() {
        assertThat(LicensePlateFormatter.normalize(null)).isNull();
        assertThat(LicensePlateFormatter.normalize("  ")).isNull();
    }

    @Test
    void shouldTrimWhitespace() {
        assertThat(LicensePlateFormatter.normalize("  ABC1D23  ")).isEqualTo("ABC1D23");
    }
}
