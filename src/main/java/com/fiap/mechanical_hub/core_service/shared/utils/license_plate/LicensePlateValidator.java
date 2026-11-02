package com.fiap.mechanical_hub.core_service.shared.utils.license_plate;

import com.fiap.mechanical_hub.core_service.domain.enums.LicensePlatePatternEnum;
import com.fiap.mechanical_hub.core_service.domain.exceptions.InvalidLicensePlateException;
import static com.fiap.mechanical_hub.core_service.shared.utils.license_plate.LicensePlateFormatter.normalize;

public class LicensePlateValidator {

    private LicensePlateValidator() {}

    public static void validateLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new InvalidLicensePlateException("Placa vazia ou nula! É necessário informar uma placa válida");
        }

        String normalized = normalize(licensePlate);

        boolean isValid = false;
        for (LicensePlatePatternEnum pattern : LicensePlatePatternEnum.values()) {
            if (pattern.matches(normalized)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new InvalidLicensePlateException(
                    "Formato de placa inválido. Use o padrão antigo (ABC1234) ou padrão Mercosul (ABC1D23)."
            );
        }
    }
}
