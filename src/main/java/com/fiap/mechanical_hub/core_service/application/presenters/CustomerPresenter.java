package com.fiap.mechanical_hub.core_service.application.presenters;

import com.fiap.mechanical_hub.core_service.application.dto.customer.CustomerResponse;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;

import java.util.List;

public interface CustomerPresenter {
    CustomerResponse toResponse(Customer customer);
    List<CustomerResponse> toResponseList(List<Customer> customers);
}
