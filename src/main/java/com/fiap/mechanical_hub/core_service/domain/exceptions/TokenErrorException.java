package com.fiap.mechanical_hub.core_service.domain.exceptions;

public class TokenErrorException extends RuntimeException {
    public TokenErrorException(String message, Exception cause) {
        super(message, cause);
    }
}
