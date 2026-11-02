package com.fiap.mechanical_hub.core_service.domain.exceptions;

public class DuplicateLicensePlateException extends BusinessRuleException {
    public DuplicateLicensePlateException(String message) {
        super(message);
    }
}
