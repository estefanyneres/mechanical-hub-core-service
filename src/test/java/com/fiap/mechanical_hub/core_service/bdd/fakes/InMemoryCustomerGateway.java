package com.fiap.mechanical_hub.core_service.bdd.fakes;

import com.fiap.mechanical_hub.core_service.application.gateways.CustomerGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryCustomerGateway implements CustomerGateway {

    private final Map<UUID, Customer> store = new LinkedHashMap<>();

    @Override
    public Customer save(Customer customer) {
        store.put(customer.getId(), customer);
        return customer;
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Customer> findByDocumentNumber(String documentNumber) {
        return store.values().stream()
                .filter(c -> c.getDocumentNumber().equals(documentNumber))
                .findFirst();
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(UUID id) {
        store.remove(id);
    }

    @Override
    public boolean existsByDocumentNumber(String documentNumber) {
        return findByDocumentNumber(documentNumber).isPresent();
    }

    @Override
    public boolean existsByDocumentNumberAndIdNot(String documentNumber, UUID id) {
        return store.values().stream()
                .anyMatch(c -> c.getDocumentNumber().equals(documentNumber) && !c.getId().equals(id));
    }
}
