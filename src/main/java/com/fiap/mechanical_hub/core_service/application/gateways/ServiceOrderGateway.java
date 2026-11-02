package com.fiap.mechanical_hub.core_service.application.gateways;

import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderGateway {
    ServiceOrder save(ServiceOrder serviceOrder);
    Optional<String> findLastOrderNumberByYearMonth(String yearMonth);
    Optional<ServiceOrder> findById(UUID id);
    List<ServiceOrder> findAll();
    void deleteById(UUID id);
    List<ServiceOrder> findAllByOrderByCreatedAtDesc();
    Optional<ServiceOrder> findByOrderNumber(String orderNumber);
    List<ServiceOrder> findActiveOrdersSortedByPriority();
}
