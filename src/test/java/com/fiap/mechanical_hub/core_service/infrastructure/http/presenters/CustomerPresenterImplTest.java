package com.fiap.mechanical_hub.core_service.infrastructure.http.presenters;

import com.fiap.mechanical_hub.core_service.application.dto.customer.CustomerResponse;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import com.fiap.mechanical_hub.core_service.domain.entities.mocks.CustomerMock;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerPresenterImplTest {

    private final CustomerPresenterImpl presenter = new CustomerPresenterImpl();

    @Test
    void shouldConvertCustomerToResponse() {
        Customer customer = CustomerMock.defaultCustomer();

        CustomerResponse response = presenter.toResponse(customer);

        assertEquals(customer.getId(), response.id());
        assertEquals(customer.getName(), response.name());
        assertEquals("CPF", response.documentType());
        assertNotNull(response.documentNumber());
        assertNotNull(response.telephone());
    }

    @Test
    void shouldConvertListOfCustomersToResponseList() {
        List<CustomerResponse> result = presenter.toResponseList(List.of(CustomerMock.defaultCustomer()));

        assertEquals(1, result.size());
    }
}
