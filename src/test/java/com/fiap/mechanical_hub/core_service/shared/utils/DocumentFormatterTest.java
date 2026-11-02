package com.fiap.mechanical_hub.core_service.shared.utils;

import com.fiap.mechanical_hub.core_service.shared.utils.document.DocumentFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class DocumentFormatterTest {

    @Test
    void formatDocument_CPFSuccess() {
        assertEquals("123.456.789-01", DocumentFormatter.formatDocument("CPF", "12345678901"));
    }

    @Test
    void formatDocument_CPFCaseInsensitive() {
        assertEquals("123.456.789-01", DocumentFormatter.formatDocument("cpf", "12345678901"));
    }

    @Test
    void formatDocument_CNPJSuccess() {
        assertEquals("12.345.678/0001-99", DocumentFormatter.formatDocument("CNPJ", "12345678000199"));
    }

    @ParameterizedTest
    @CsvSource({"CPF, 123", "CNPJ, 12345"})
    void formatDocument_InvalidLength(String type, String number) {
        assertThrows(IllegalArgumentException.class, () -> DocumentFormatter.formatDocument(type, number));
    }

    @Test
    void formatDocument_NullInputs() {
        assertNull(DocumentFormatter.formatDocument(null, null));
        assertEquals("123", DocumentFormatter.formatDocument(null, "123"));
        assertNull(DocumentFormatter.formatDocument("CPF", null));
    }

    @Test
    void formatDocument_RemoveExistingFormatting() {
        assertEquals("123.456.789-01", DocumentFormatter.formatDocument("CPF", "123.456.789-01"));
        assertEquals("12.345.678/0001-99", DocumentFormatter.formatDocument("CNPJ", "12.345.678/0001-99"));
    }

    @Test
    void formatCPF_ExceptionMessage() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> DocumentFormatter.formatDocument("CPF", "123"));
        assertEquals("CPF deve ter 11 dígitos", exception.getMessage());
    }

    @Test
    void formatCNPJ_ExceptionMessage() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> DocumentFormatter.formatDocument("CNPJ", "123"));
        assertEquals("CNPJ deve ter 14 dígitos", exception.getMessage());
    }
}
