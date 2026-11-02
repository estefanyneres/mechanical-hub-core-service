package com.fiap.mechanical_hub.core_service.domain.exceptions;

public class InvalidOrderTransitionException extends BusinessRuleException {
    public InvalidOrderTransitionException(String message) {
        super(message);
    }
}
