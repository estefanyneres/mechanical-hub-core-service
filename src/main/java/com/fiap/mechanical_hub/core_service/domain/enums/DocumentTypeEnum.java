package com.fiap.mechanical_hub.core_service.domain.enums;

public enum DocumentTypeEnum {
    CPF("CPF"),
    CNPJ("CNPJ");

    private final String value;

    DocumentTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() { return value; }

    public static DocumentTypeEnum fromValue(String value) {
        for (DocumentTypeEnum type : DocumentTypeEnum.values()) {
            if (type.value.equalsIgnoreCase(value)) { return type; }
        }
        throw new IllegalArgumentException("Tipo de documento inválido: " + value);
    }
}
