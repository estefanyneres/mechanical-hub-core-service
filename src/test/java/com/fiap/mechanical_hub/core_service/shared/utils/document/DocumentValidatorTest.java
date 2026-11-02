package com.fiap.mechanical_hub.core_service.shared.utils.document;

import com.fiap.mechanical_hub.core_service.domain.enums.DocumentTypeEnum;
import com.fiap.mechanical_hub.core_service.domain.exceptions.InvalidDocumentException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentValidatorTest {

    @Test
    void shouldValidateCpf() {
        assertThatCode(() -> DocumentValidator.validateDocument(DocumentTypeEnum.CPF, "52998224725"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectInvalidCpf() {
        assertThatThrownBy(() -> DocumentValidator.validateDocument(DocumentTypeEnum.CPF, "12345678901"))
                .isInstanceOf(InvalidDocumentException.class);
    }

    @Test
    void shouldRejectRepeatedDigitsCpf() {
        assertThatThrownBy(() -> DocumentValidator.validateDocument(DocumentTypeEnum.CPF, "11111111111"))
                .isInstanceOf(InvalidDocumentException.class);
    }

    @Test
    void shouldRejectNullCpf() {
        assertThatThrownBy(() -> DocumentValidator.validateDocument(DocumentTypeEnum.CPF, null))
                .isInstanceOf(InvalidDocumentException.class);
    }

    @Test
    void shouldValidateCnpj() {
        assertThatCode(() -> DocumentValidator.validateDocument(DocumentTypeEnum.CNPJ, "11222333000181"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectInvalidCnpj() {
        assertThatThrownBy(() -> DocumentValidator.validateDocument(DocumentTypeEnum.CNPJ, "11222333000100"))
                .isInstanceOf(InvalidDocumentException.class);
    }

    @Test
    void shouldRejectRepeatedDigitsCnpj() {
        assertThatThrownBy(() -> DocumentValidator.validateDocument(DocumentTypeEnum.CNPJ, "11111111111111"))
                .isInstanceOf(InvalidDocumentException.class);
    }

    @Test
    void shouldRejectNullCnpj() {
        assertThatThrownBy(() -> DocumentValidator.validateDocument(DocumentTypeEnum.CNPJ, null))
                .isInstanceOf(InvalidDocumentException.class);
    }
}
