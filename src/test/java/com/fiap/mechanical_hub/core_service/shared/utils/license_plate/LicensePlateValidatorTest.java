package com.fiap.mechanical_hub.core_service.shared.utils.license_plate;

import com.fiap.mechanical_hub.core_service.domain.exceptions.InvalidLicensePlateException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LicensePlateValidatorTest {

    @Test
    void shouldAcceptAntigoPattern() {
        assertThatCode(() -> LicensePlateValidator.validateLicensePlate("ABC1234")).doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptMercosulPattern() {
        assertThatCode(() -> LicensePlateValidator.validateLicensePlate("ABC1D23")).doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptWithHyphen() {
        assertThatCode(() -> LicensePlateValidator.validateLicensePlate("ABC-1234")).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectInvalidPlate() {
        assertThatThrownBy(() -> LicensePlateValidator.validateLicensePlate("INVALIDA"))
                .isInstanceOf(InvalidLicensePlateException.class);
    }

    @Test
    void shouldRejectNull() {
        assertThatThrownBy(() -> LicensePlateValidator.validateLicensePlate(null))
                .isInstanceOf(InvalidLicensePlateException.class);
    }

    @Test
    void shouldRejectEmpty() {
        assertThatThrownBy(() -> LicensePlateValidator.validateLicensePlate(""))
                .isInstanceOf(InvalidLicensePlateException.class);
    }
}
