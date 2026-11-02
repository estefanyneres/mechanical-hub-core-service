package com.fiap.mechanical_hub.core_service.infrastructure.http.exception;

import com.fiap.mechanical_hub.core_service.domain.exceptions.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";
    public static final String BAD_REQUEST = "Requisição Inválida";

    private final MeterRegistry registry;

    @ExceptionHandler(InvalidDocumentException.class)
    public ResponseEntity<Object> handleInvalidDocument(InvalidDocumentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(STATUS, HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put(ERROR, "Documento Inválido");
        body.put(MESSAGE, ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(InvalidTelephoneException.class)
    public ResponseEntity<Object> handleInvalidTelephone(InvalidTelephoneException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(STATUS, HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put(ERROR, "Telefone Inválido");
        body.put(MESSAGE, ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(DuplicateDocumentException.class)
    public ResponseEntity<Object> handleDuplicateDocument(DuplicateDocumentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(STATUS, HttpStatus.CONFLICT.value());
        body.put(ERROR, "Documento duplicado");
        body.put(MESSAGE, ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DuplicateLicensePlateException.class)
    public ResponseEntity<Object> handleDuplicateLicensePlate(DuplicateLicensePlateException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(STATUS, HttpStatus.CONFLICT.value());
        body.put(ERROR, "Placa duplicada");
        body.put(MESSAGE, ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidLicensePlateException.class)
    public ResponseEntity<Object> handleInvalidLicensePlate(InvalidLicensePlateException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(STATUS, HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put(ERROR, "Placa inválida");
        body.put(MESSAGE, ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Object> handleBusinessRuleException(BusinessRuleException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(STATUS, HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put(ERROR, "Violação de Regra de Negócio");
        body.put(MESSAGE, ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(STATUS, HttpStatus.NOT_FOUND.value());
        body.put(ERROR, "Não Encontrado");
        body.put(MESSAGE, ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(STATUS, HttpStatus.BAD_REQUEST.value());
        body.put(ERROR, BAD_REQUEST);
        body.put(MESSAGE, "Corpo da requisição ausente ou com formato inválido");

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(STATUS, HttpStatus.BAD_REQUEST.value());
        body.put(ERROR, BAD_REQUEST);
        body.put(MESSAGE, ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNoSuchElement(NoSuchElementException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(STATUS, HttpStatus.BAD_REQUEST.value());
        body.put(ERROR, BAD_REQUEST);
        body.put(MESSAGE, ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidOrderTransitionException.class)
    public ResponseEntity<Object> handleInvalidOrderTransition(InvalidOrderTransitionException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(STATUS, HttpStatus.BAD_REQUEST.value());
        body.put(ERROR, BAD_REQUEST);
        body.put(MESSAGE, ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDataAccessException(DataAccessException ex) {
        log.error("Falha na integração com o banco de dados", ex);
        Counter.builder("integration.failure")
                .tag("type", "database")
                .register(registry)
                .increment();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(STATUS, HttpStatus.SERVICE_UNAVAILABLE.value());
        body.put(ERROR, "Falha na integração com o banco de dados");
        body.put(MESSAGE, "Não foi possível completar a operação devido a uma falha de infraestrutura");

        return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
    }

}
