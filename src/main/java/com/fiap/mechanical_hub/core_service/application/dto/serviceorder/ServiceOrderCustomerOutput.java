package com.fiap.mechanical_hub.core_service.application.dto.serviceorder;

import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;

public record ServiceOrderCustomerOutput(ServiceOrder order, Vehicle vehicle, Customer customer) {}
