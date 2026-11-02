package com.fiap.mechanical_hub.core_service.application.dto.serviceorder;

import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;

public record ServiceOrderSummaryOutput(ServiceOrder order, Customer customer, Vehicle vehicle) {}
