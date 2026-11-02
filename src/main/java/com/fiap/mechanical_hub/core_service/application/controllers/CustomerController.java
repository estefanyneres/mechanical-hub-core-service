package com.fiap.mechanical_hub.core_service.application.controllers;

import com.fiap.mechanical_hub.core_service.application.dto.customer.CustomerResponse;
import com.fiap.mechanical_hub.core_service.application.dto.customer.UpsertCustomerRequest;
import com.fiap.mechanical_hub.core_service.application.gateways.CustomerGateway;
import com.fiap.mechanical_hub.core_service.application.mappers.CustomerMapper;
import com.fiap.mechanical_hub.core_service.application.presenters.CustomerPresenter;
import com.fiap.mechanical_hub.core_service.application.usecases.CustomerUseCase;

import java.util.List;
import java.util.UUID;

public class CustomerController {

    private final CustomerUseCase customerUseCase;

    public CustomerController(CustomerGateway customerGateway, CustomerMapper customerMapper,
                              CustomerPresenter customerPresenter) {
        this.customerUseCase = new CustomerUseCase(customerGateway, customerMapper, customerPresenter);
    }

    public CustomerResponse create(UpsertCustomerRequest request) {
        return customerUseCase.create(request);
    }

    public List<CustomerResponse> findAll() {
        return customerUseCase.findAll();
    }

    public CustomerResponse findById(UUID id) {
        return customerUseCase.findByIdAndPresent(id);
    }

    public CustomerResponse update(UUID id, UpsertCustomerRequest request) {
        return customerUseCase.update(id, request);
    }

    public void delete(UUID id) {
        customerUseCase.delete(id);
    }
}
