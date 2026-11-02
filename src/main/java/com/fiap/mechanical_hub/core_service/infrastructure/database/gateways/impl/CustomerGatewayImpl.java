package com.fiap.mechanical_hub.core_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.core_service.application.gateways.CustomerGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.CustomerModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.repositories.CustomerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerGatewayImpl implements CustomerGateway {

    private final CustomerJpaRepository jpaRepository;

    @Override
    public Customer save(Customer customer) {
        CustomerModel entity = toJpaEntity(customer);
        CustomerModel saved = jpaRepository.save(entity);
        return toDomainEntity(saved);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomainEntity);
    }

    @Override
    public Optional<Customer> findByDocumentNumber(String documentNumber) {
        return jpaRepository.findByDocumentNumber(documentNumber).map(this::toDomainEntity);
    }

    @Override
    public List<Customer> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomainEntity)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByDocumentNumber(String documentNumber) {
        return jpaRepository.existsByDocumentNumber(documentNumber);
    }

    @Override
    public boolean existsByDocumentNumberAndIdNot(String documentNumber, UUID id) {
        return jpaRepository.existsByDocumentNumberAndIdNot(documentNumber, id);
    }

    private CustomerModel toJpaEntity(Customer customer) {
        return new CustomerModel(
                customer.getId(),
                customer.getName(),
                customer.getDocumentTypeEnum(),
                customer.getDocumentNumber(),
                customer.getTelephone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    private Customer toDomainEntity(CustomerModel entity) {
        return new Customer(
                entity.getId(),
                entity.getName(),
                entity.getDocumentTypeEnum(),
                entity.getDocumentNumber(),
                entity.getTelephone(),
                entity.getEmail(),
                entity.getAddress(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
