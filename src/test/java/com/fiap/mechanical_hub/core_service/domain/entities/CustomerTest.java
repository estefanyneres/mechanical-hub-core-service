package com.fiap.mechanical_hub.core_service.domain.entities;

import com.fiap.mechanical_hub.core_service.domain.entities.constants.TestConstants;
import com.fiap.mechanical_hub.core_service.domain.entities.mocks.CustomerMock;
import com.fiap.mechanical_hub.core_service.domain.enums.DocumentTypeEnum;
import org.junit.jupiter.api.Test;

import static com.fiap.mechanical_hub.core_service.domain.entities.constants.TestConstants.DEFAULT_CUSTOMER_DOCUMENT;
import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void shouldCreateCustomerWithValidData() {
        Customer customer = CustomerMock.defaultCustomer();

        assertNotNull(customer.getId());
        assertEquals(TestConstants.DEFAULT_CUSTOMER_NAME, customer.getName());
        assertEquals(DocumentTypeEnum.CPF, customer.getDocumentTypeEnum());
        assertEquals(DEFAULT_CUSTOMER_DOCUMENT, customer.getDocumentNumber());
        assertEquals(TestConstants.DEFAULT_CUSTOMER_TELEPHONE, customer.getTelephone());
        assertEquals(TestConstants.DEFAULT_CUSTOMER_EMAIL, customer.getEmail());
        assertEquals(TestConstants.DEFAULT_CUSTOMER_ADDRESS, customer.getAddress());
        assertNotNull(customer.getCreatedAt());
        assertNotNull(customer.getUpdatedAt());
    }

    @Test
    void shouldCreateCustomerWithCNPJ() {
        Customer customer = CustomerMock.customerWithCNPJ();

        assertNotNull(customer.getId());
        assertEquals(DocumentTypeEnum.CNPJ, customer.getDocumentTypeEnum());
    }

    @Test
    void shouldUpdateCustomer() {
        Customer customer = CustomerMock.defaultCustomer();
        String newName = "Maria Silva";
        String newTelephone = "5511912345678";
        String newEmail = "maria@example.com";

        customer.update(newName, DocumentTypeEnum.CPF, DEFAULT_CUSTOMER_DOCUMENT,
                newTelephone, newEmail, TestConstants.DEFAULT_CUSTOMER_ADDRESS);

        assertEquals(newName, customer.getName());
        assertEquals(newTelephone, customer.getTelephone());
        assertEquals(newEmail, customer.getEmail());
    }

    @Test
    void shouldRemoveFormatFromDocumentWhenCreating() {
        Customer customer = CustomerMock.customerWithCustomValues(
                "João Silva", DEFAULT_CUSTOMER_DOCUMENT, "55 (11) 98765-4321", "joao@example.com", "Rua A, 123");

        assertNotNull(customer.getDocumentNumber());
        assertNotNull(customer.getTelephone());
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithInvalidDocument() {
        assertThrows(Exception.class, () -> Customer.create(
                "João Silva", DocumentTypeEnum.CPF, "invalid", "(11) 98765-4321", "joao@example.com", "Rua A, 123"));
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithInvalidTelephone() {
        assertThrows(Exception.class, () -> Customer.create(
                "João Silva", DocumentTypeEnum.CPF, DEFAULT_CUSTOMER_DOCUMENT, "invalid", "joao@example.com", "Rua A, 123"));
    }
}
