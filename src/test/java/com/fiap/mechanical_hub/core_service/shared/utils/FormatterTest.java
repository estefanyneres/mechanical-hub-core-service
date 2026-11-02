package com.fiap.mechanical_hub.core_service.shared.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormatterTest {

    @Test
    void shouldRemoveFormatting() {
        assertThat(Formatter.removeFormatting("123.456.789-01")).isEqualTo("12345678901");
        assertThat(Formatter.removeFormatting("(11) 99999-8888")).isEqualTo("11999998888");
    }

    @Test
    void shouldReturnNullForNull() {
        assertThat(Formatter.removeFormatting(null)).isNull();
    }

    @Test
    void shouldKeepCleanString() {
        assertThat(Formatter.removeFormatting("12345678901")).isEqualTo("12345678901");
    }
}
