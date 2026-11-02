package com.fiap.mechanical_hub.core_service.application.usecases;

import com.fiap.mechanical_hub.core_service.application.dto.customer.CustomerResponse;
import com.fiap.mechanical_hub.core_service.application.dto.customer.UpsertCustomerRequest;
import com.fiap.mechanical_hub.core_service.application.gateways.CustomerGateway;
import com.fiap.mechanical_hub.core_service.application.mappers.CustomerMapper;
import com.fiap.mechanical_hub.core_service.application.presenters.CustomerPresenter;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import com.fiap.mechanical_hub.core_service.domain.enums.DocumentTypeEnum;
import com.fiap.mechanical_hub.core_service.domain.exceptions.DuplicateDocumentException;
import com.fiap.mechanical_hub.core_service.domain.exceptions.InvalidDocumentException;
import com.fiap.mechanical_hub.core_service.domain.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerUseCaseTest {

    @Mock
    private CustomerGateway customerGateway;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private CustomerPresenter customerPresenter;

    @InjectMocks
    private CustomerUseCase customerUseCase;

    private UUID customerId;
    private Customer customer;
    private UpsertCustomerRequest upsertRequest;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        customer = new Customer(customerId, "João Silva", DocumentTypeEnum.CPF, "12345678901",
                "11987654321", "joao@example.com", "Rua A, 123", now, now);

        upsertRequest = new UpsertCustomerRequest(
                "João Silva", "CPF", "12345678901", "11987654321", "joao@example.com", "Rua A, 123");
    }

    @Test
    void shouldCreateCustomerSuccessfully() {
        CustomerResponse expectedResponse = new CustomerResponse(customerId, "João Silva", "CPF", "12345678901",
                "11987654321", "joao@example.com", "Rua A, 123", LocalDateTime.now(), LocalDateTime.now());
        when(customerGateway.existsByDocumentNumber("12345678901")).thenReturn(false);
        when(customerMapper.toDomainEntity(upsertRequest)).thenReturn(customer);
        when(customerGateway.save(customer)).thenReturn(customer);
        when(customerPresenter.toResponse(customer)).thenReturn(expectedResponse);

        CustomerResponse result = customerUseCase.create(upsertRequest);

        assertNotNull(result);
        assertEquals("João Silva", result.name());
        verify(customerGateway).save(customer);
    }

    @Test
    void shouldThrowDuplicateDocumentWhenCreatingWithExistingDocument() {
        when(customerGateway.existsByDocumentNumber("12345678901")).thenReturn(true);

        assertThrows(DuplicateDocumentException.class, () -> customerUseCase.create(upsertRequest));
        verify(customerGateway, never()).save(any());
    }

    @Test
    void shouldFindCustomerByIdSuccessfully() {
        when(customerGateway.findById(customerId)).thenReturn(Optional.of(customer));

        Customer result = customerUseCase.findById(customerId);

        assertEquals(customerId, result.getId());
    }

    @Test
    void shouldThrowNotFoundWhenCustomerNotFoundById() {
        when(customerGateway.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> customerUseCase.findById(customerId));
    }

    @Test
    void shouldFindAllCustomers() {
        List<Customer> customers = List.of(customer);
        CustomerResponse response = new CustomerResponse(customerId, "João Silva", "CPF", "12345678901",
                "11987654321", "joao@example.com", "Rua A, 123", LocalDateTime.now(), LocalDateTime.now());
        when(customerGateway.findAll()).thenReturn(customers);
        when(customerPresenter.toResponseList(customers)).thenReturn(List.of(response));

        List<CustomerResponse> result = customerUseCase.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void shouldUpdateCustomerSuccessfully() {
        UpsertCustomerRequest updateRequest = new UpsertCustomerRequest(
                "João Silva Atualizado", "CPF", "12345678901", "11987654321", "joao@example.com", "Rua B, 456");
        Customer updatedCustomer = new Customer(customerId, "João Silva Atualizado", DocumentTypeEnum.CPF,
                "12345678901", "11987654321", "joao@example.com", "Rua B, 456", customer.getCreatedAt(), LocalDateTime.now());
        CustomerResponse expectedResponse = new CustomerResponse(customerId, "João Silva Atualizado", "CPF",
                "12345678901", "11987654321", "joao@example.com", "Rua B, 456", LocalDateTime.now(), LocalDateTime.now());

        when(customerGateway.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerGateway.existsByDocumentNumberAndIdNot("12345678901", customerId)).thenReturn(false);
        when(customerMapper.toDomainEntity(updateRequest, customer)).thenReturn(updatedCustomer);
        when(customerGateway.save(updatedCustomer)).thenReturn(updatedCustomer);
        when(customerPresenter.toResponse(updatedCustomer)).thenReturn(expectedResponse);

        CustomerResponse result = customerUseCase.update(customerId, updateRequest);

        assertNotNull(result);
        verify(customerGateway).save(updatedCustomer);
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentCustomer() {
        when(customerGateway.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> customerUseCase.update(customerId, upsertRequest));
        verify(customerGateway, never()).save(any());
    }

    @Test
    void shouldThrowInvalidDocumentWhenTryingToChangeDocument() {
        UpsertCustomerRequest updateRequest = new UpsertCustomerRequest(
                "João Silva", "CPF", "98765432100", "11987654321", "joao@example.com", "Rua A, 123");
        when(customerGateway.findById(customerId)).thenReturn(Optional.of(customer));

        InvalidDocumentException exception = assertThrows(InvalidDocumentException.class,
                () -> customerUseCase.update(customerId, updateRequest));
        assertEquals("Não é permitido alterar o documento do cliente", exception.getMessage());
        verify(customerGateway, never()).save(any());
    }

    @Test
    void shouldDeleteCustomerSuccessfully() {
        when(customerGateway.findById(customerId)).thenReturn(Optional.of(customer));

        assertDoesNotThrow(() -> customerUseCase.delete(customerId));

        verify(customerGateway).deleteById(customerId);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentCustomer() {
        when(customerGateway.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> customerUseCase.delete(customerId));
        verify(customerGateway, never()).deleteById(any());
    }

    @Test
    void shouldFindExistingCustomerByDocument() {
        when(customerGateway.findByDocumentNumber("12345678901")).thenReturn(Optional.of(customer));

        Customer result = customerUseCase.findByDocumentOrCreate(
                "João Silva", "CPF", "123.456.789-01", "(11) 98765-4321", "joao@example.com", "Rua A, 123");

        assertEquals(customer, result);
        verify(customerGateway, never()).save(any());
    }

    @Test
    void shouldCreateNewCustomerWhenNotFoundByDocument() {
        String documentoFormatado = "111.444.777-35";
        String documentoLimpo = "11144477735";

        when(customerGateway.findByDocumentNumber(documentoLimpo)).thenReturn(Optional.empty());
        when(customerGateway.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        Customer result = customerUseCase.findByDocumentOrCreate(
                "João Silva", "CPF", documentoFormatado, "5511987654321", "joao@example.com", "Rua A, 123");

        assertThat(result).isNotNull();
        assertThat(result.getDocumentNumber()).isEqualTo(documentoLimpo);
        verify(customerGateway).save(any(Customer.class));
    }

    @Test
    void shouldThrowDuplicateDocumentExceptionWhenDocumentExistsForAnotherIdOnUpdate() {
        String cleanDocument = "12345678900";
        UpsertCustomerRequest request = new UpsertCustomerRequest(null, null, "123.456.789-00", null, null, null);
        Customer existingCustomer = new Customer(UUID.randomUUID(), "João Silva Atualizado", DocumentTypeEnum.CPF,
                cleanDocument, "11987654321", "joao@example.com", "Rua B, 456", customer.getCreatedAt(), LocalDateTime.now());

        when(customerGateway.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerGateway.existsByDocumentNumberAndIdNot(cleanDocument, customerId)).thenReturn(true);

        assertThrows(DuplicateDocumentException.class, () -> customerUseCase.update(customerId, request));
    }
}
