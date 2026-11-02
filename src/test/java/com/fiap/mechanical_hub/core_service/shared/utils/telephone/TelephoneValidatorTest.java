package com.fiap.mechanical_hub.core_service.shared.utils.telephone;

import com.fiap.mechanical_hub.core_service.domain.exceptions.InvalidTelephoneException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelephoneValidatorTest {

    @Test
    void shouldAcceptValidTelephone() {
        assertThatCode(() -> TelephoneValidator.validateTelephone("5511999998888")).doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptFormattedTelephone() {
        assertThatCode(() -> TelephoneValidator.validateTelephone("+55 (11) 99999-8888")).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectShortTelephone() {
        assertThatThrownBy(() -> TelephoneValidator.validateTelephone("11999998"))
                .isInstanceOf(InvalidTelephoneException.class);
    }

    @Test
    void shouldRejectNull() {
        assertThatThrownBy(() -> TelephoneValidator.validateTelephone(null))
                .isInstanceOf(InvalidTelephoneException.class);
    }

    @Test
    void isValidShouldReturnFalseForNull() {
        assertThat(TelephoneValidator.isValidTelephone(null)).isFalse();
    }

    @Test
    void isValidShouldReturnTrueForValid() {
        assertThat(TelephoneValidator.isValidTelephone("5511999998888")).isTrue();
    }
}
