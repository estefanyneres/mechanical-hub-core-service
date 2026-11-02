package com.fiap.mechanical_hub.core_service.shared.utils.document;

import static com.fiap.mechanical_hub.core_service.shared.utils.Formatter.removeFormatting;

public class DocumentFormatter {

    private DocumentFormatter() {}

    public static String formatDocument(String documentType, String documentNumber) {
        if (documentType == null || documentNumber == null) {
            return documentNumber;
        }
        return documentType.equalsIgnoreCase("CPF")
                ? formatCPF(documentNumber)
                : formatCNPJ(documentNumber);
    }

    private static String formatCPF(String cpf) {
        if (cpf == null) {
            return null;
        }
        String cleanCpf = removeFormatting(cpf);
        if (cleanCpf.length() != 11) {
            throw new IllegalArgumentException("CPF deve ter 11 dígitos");
        }
        return String.format("%s.%s.%s-%s",
                cleanCpf.substring(0, 3),
                cleanCpf.substring(3, 6),
                cleanCpf.substring(6, 9),
                cleanCpf.substring(9, 11));
    }

    private static String formatCNPJ(String cnpj) {
        if (cnpj == null) {
            return null;
        }
        String cleanCnpj = removeFormatting(cnpj);
        if (cleanCnpj.length() != 14) {
            throw new IllegalArgumentException("CNPJ deve ter 14 dígitos");
        }
        return String.format("%s.%s.%s/%s-%s",
                cleanCnpj.substring(0, 2),
                cleanCnpj.substring(2, 5),
                cleanCnpj.substring(5, 8),
                cleanCnpj.substring(8, 12),
                cleanCnpj.substring(12, 14));
    }
}
