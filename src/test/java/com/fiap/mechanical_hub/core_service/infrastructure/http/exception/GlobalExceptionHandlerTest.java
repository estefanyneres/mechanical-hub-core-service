package com.fiap.mechanical_hub.core_service.infrastructure.http.exception;

import com.fiap.mechanical_hub.core_service.domain.exceptions.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(new SimpleMeterRegistry());

    @Test
    void shouldHandleInvalidDocument() {
        ResponseEntity<Object> response = handler.handleInvalidDocument(new InvalidDocumentException("doc inválido"));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("doc inválido", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    void shouldHandleInvalidTelephone() {
        ResponseEntity<Object> response = handler.handleInvalidTelephone(new InvalidTelephoneException("tel inválido"));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    void shouldHandleDuplicateDocument() {
        ResponseEntity<Object> response = handler.handleDuplicateDocument(new DuplicateDocumentException("duplicado"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void shouldHandleDuplicateLicensePlate() {
        ResponseEntity<Object> response = handler.handleDuplicateLicensePlate(new DuplicateLicensePlateException("placa duplicada"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void shouldHandleInvalidLicensePlate() {
        ResponseEntity<Object> response = handler.handleInvalidLicensePlate(new InvalidLicensePlateException("placa inválida"));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    void shouldHandleBusinessRuleException() {
        ResponseEntity<Object> response = handler.handleBusinessRuleException(new BusinessRuleException("regra violada"));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    void shouldHandleNotFound() {
        ResponseEntity<Object> response = handler.handleNotFound(new NotFoundException("não encontrado"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldHandleHttpMessageNotReadable() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("corpo inválido", (org.springframework.http.HttpInputMessage) null);

        ResponseEntity<Object> response = handler.handleHttpMessageNotReadable(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldHandleIllegalArgument() {
        ResponseEntity<Object> response = handler.handleIllegalArgument(new IllegalArgumentException("argumento inválido"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldHandleNoSuchElement() {
        ResponseEntity<Object> response = handler.handleNoSuchElement(new NoSuchElementException("não existe"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldHandleInvalidOrderTransition() {
        ResponseEntity<Object> response = handler.handleInvalidOrderTransition(new InvalidOrderTransitionException("transição inválida"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldHandleDataAccessException() {
        DataAccessException ex = new DataAccessException("falha de infra") {};

        ResponseEntity<Object> response = handler.handleDataAccessException(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }
}
