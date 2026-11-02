package com.fiap.mechanical_hub.core_service.shared.utils.document;

import com.fiap.mechanical_hub.core_service.domain.enums.DocumentTypeEnum;
import com.fiap.mechanical_hub.core_service.domain.exceptions.InvalidDocumentException;

public class DocumentValidator {

    private DocumentValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void validateDocument(DocumentTypeEnum documentTypeEnum, String documentNumber) {
        boolean isValid = documentTypeEnum == DocumentTypeEnum.CPF
                ? isValidCPF(documentNumber)
                : isValidCNPJ(documentNumber);

        if (!isValid) {
            throw new InvalidDocumentException(
                    String.format("Inválido %s: %s", documentTypeEnum.getValue(), documentNumber)
            );
        }
    }

    private static boolean isValidCPF(String cpf) {
        if (cpf == null) { return false; }

        String cleanCpf = cpf.replaceAll("\\D", "");

        if (cleanCpf.length() != 11) { return false; }

        if (cleanCpf.matches("(\\d)\\1{10}")) { return false; }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cleanCpf.charAt(i) - '0') * (10 - i);
        }
        int firstCheckDigit = 11 - (sum % 11);
        if (firstCheckDigit >= 10) { firstCheckDigit = 0; }

        if ((cleanCpf.charAt(9) - '0') != firstCheckDigit) { return false; }

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cleanCpf.charAt(i) - '0') * (11 - i);
        }
        int secondCheckDigit = 11 - (sum % 11);
        if (secondCheckDigit >= 10) {
            secondCheckDigit = 0;
        }

        return (cleanCpf.charAt(10) - '0') == secondCheckDigit;
    }

    private static boolean isValidCNPJ(String cnpj) {
        if (cnpj == null) return false;

        String clean = cnpj.replaceAll("\\D", "");

        if (clean.length() != 14) return false;
        if (clean.matches("(\\d)\\1{13}")) return false;

        int[] weights1 = {5,4,3,2,9,8,7,6,5,4,3,2};
        int[] weights2 = {6,5,4,3,2,9,8,7,6,5,4,3,2};

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += (clean.charAt(i) - '0') * weights1[i];
        }

        int firstDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);

        if ((clean.charAt(12) - '0') != firstDigit) return false;

        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += (clean.charAt(i) - '0') * weights2[i];
        }

        int secondDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);

        return (clean.charAt(13) - '0') == secondDigit;
    }
}
