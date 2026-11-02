package com.fiap.mechanical_hub.core_service.shared.utils.telephone;

import static com.fiap.mechanical_hub.core_service.shared.utils.Formatter.removeFormatting;

public class TelephoneFormatter {

    private TelephoneFormatter() {}

    public static String formatTelephone(String telephone) {
        if (telephone == null) {
            return null;
        }

        String cleanTelephone = removeFormatting(telephone);

        return switch (cleanTelephone.length()) {
            case 10 -> formatLandline10Digits(cleanTelephone);
            case 11 -> formatMobile11Digits(cleanTelephone);
            case 12 -> formatWithCountryCode12Digits(cleanTelephone);
            case 13 -> formatWithCountryCode13Digits(cleanTelephone);
            default -> throw new IllegalArgumentException(
                    "Telefone inválido: após remover a formatação, deve conter 10, 11, 12 ou 13 dígitos. " +
                    "Recebido: " + cleanTelephone.length() + " dígitos"
            );
        };
    }

    private static String formatLandline10Digits(String telephone) {
        return String.format("(%s) %s-%s",
                telephone.substring(0, 2),
                telephone.substring(2, 6),
                telephone.substring(6, 10));
    }

    private static String formatMobile11Digits(String telephone) {
        return String.format("(%s) %s %s-%s",
                telephone.substring(0, 2),
                telephone.substring(2, 3),
                telephone.substring(3, 7),
                telephone.substring(7, 11));
    }

    private static String formatWithCountryCode12Digits(String telephone) {
        return String.format("+55 (%s) %s-%s",
                telephone.substring(2, 4),
                telephone.substring(4, 8),
                telephone.substring(8, 12));
    }

    private static String formatWithCountryCode13Digits(String telephone) {
        return String.format("+55 (%s) %s %s-%s",
                telephone.substring(2, 4),
                telephone.substring(4, 5),
                telephone.substring(5, 9),
                telephone.substring(9, 13));
    }
}
