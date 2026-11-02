package com.fiap.mechanical_hub.core_service.shared.utils.license_plate;

public class LicensePlateFormatter {

    private LicensePlateFormatter() {}

    public static String normalize(String licensePlate) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            return null;
        }
        return licensePlate
                .trim()
                .toUpperCase()
                .replace("-", "");
    }
}
