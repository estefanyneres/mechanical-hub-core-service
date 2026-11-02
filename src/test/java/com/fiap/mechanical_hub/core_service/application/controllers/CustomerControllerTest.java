package com.fiap.mechanical_hub.core_service.application.controllers;

import com.fiap.mechanical_hub.core_service.application.dto.customer.CustomerResponse;
import com.fiap.mechanical_hub.core_service.application.dto.customer.UpsertCustomerRequest;
import com.fiap.mechanical_hub.core_service.application.gateways.CustomerGateway;
import com.fiap.mechanical_hub.core_service.application.mappers.CustomerMapper;
import com.fiap.mechanical_hub.core_service.application.presenters.CustomerPresenter;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock private CustomerGateway customerGateway;
    @Mock private CustomerMapper customerMapper;
    @Mock private CustomerPresenter customerPresenter;

    private CustomerController controller;

    @Test
    void shouldDelegateCreateToUseCase() {
        controller = new CustomerController(customerGateway, customerMapper, customerPresenter);
        UpsertCustomerRequest request = new UpsertCustomerRequest("João", "CPF", "52998224725", "5511987654321", "j@x.com", "Rua A");
        Customer customer = Customer.create("João", com.fiap.mechanical_hub.core_service.domain.enums.DocumentTypeEnum.CPF,
                "52998224725", "5511987654321", "j@x.com", "Rua A");
        CustomerResponse response = new CustomerResponse(UUID.randomUUID(), "João", "CPF", "52998224725",
                "5511987654321", "j@x.com", "Rua A", null, null);

        when(customerGateway.existsByDocumentNumber("52998224725")).thenReturn(false);
        when(customerMapper.toDomainEntity(request)).thenReturn(customer);
        when(customerGateway.save(customer)).thenReturn(customer);
        when(customerPresenter.toResponse(customer)).thenReturn(response);

        CustomerResponse result = controller.create(request);

        assertEquals(response, result);
    }

    @Test
    void shouldDelegateFindAllToUseCase() {
        controller = new CustomerController(customerGateway, customerMapper, customerPresenter);
        when(customerGateway.findAll()).thenReturn(List.of());
        when(customerPresenter.toResponseList(List.of())).thenReturn(List.of());

        List<CustomerResponse> result = controller.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDelegateFindByIdToUseCase() {
        controller = new CustomerController(customerGateway, customerMapper, customerPresenter);
        UUID id = UUID.randomUUID();
        Customer customer = mock(Customer.class);
        CustomerResponse response = mock(CustomerResponse.class);
        when(customerGateway.findById(id)).thenReturn(Optional.of(customer));
        when(customerPresenter.toResponse(customer)).thenReturn(response);

        assertEquals(response, controller.findById(id));
    }

    @Test
    void shouldDelegateUpdateToUseCase() {
        controller = new CustomerController(customerGateway, customerMapper, customerPresenter);
        UUID id = UUID.randomUUID();
        UpsertCustomerRequest request = new UpsertCustomerRequest(null, null, "52998224725", null, null, null);
        Customer existing = Customer.create("João", com.fiap.mechanical_hub.core_service.domain.enums.DocumentTypeEnum.CPF,
                "52998224725", "5511987654321", "j@x.com", "Rua A");
        CustomerResponse response = mock(CustomerResponse.class);

        when(customerGateway.findById(id)).thenReturn(Optional.of(existing));
        when(customerGateway.existsByDocumentNumberAndIdNot("52998224725", id)).thenReturn(false);
        when(customerMapper.toDomainEntity(request, existing)).thenReturn(existing);
        when(customerGateway.save(existing)).thenReturn(existing);
        when(customerPresenter.toResponse(existing)).thenReturn(response);

        assertEquals(response, controller.update(id, request));
    }

    @Test
    void shouldDelegateDeleteToUseCase() {
        controller = new CustomerController(customerGateway, customerMapper, customerPresenter);
        UUID id = UUID.randomUUID();
        when(customerGateway.findById(id)).thenReturn(Optional.of(mock(Customer.class)));

        controller.delete(id);

        verify(customerGateway).deleteById(id);
    }
}
