package com.fiap.mechanical_hub.core_service.application.gateways;

import com.fiap.mechanical_hub.core_service.domain.entities.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerGateway {

    Customer save(Customer customer);

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByDocumentNumber(String documentNumber);

    List<Customer> findAll();

    void deleteById(UUID id);

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumberAndIdNot(String documentNumber, UUID id);
}
