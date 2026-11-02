package com.fiap.mechanical_hub.core_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.infrastructure.database.mappers.ServiceOrderJpaMapper;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.ServiceOrderModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.repositories.ServiceOrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServiceOrderGatewayImpl implements ServiceOrderGateway {

    private final ServiceOrderJpaRepository jpaRepository;

    @Override
    public ServiceOrder save(ServiceOrder order) {
        ServiceOrderModel entity = ServiceOrderJpaMapper.toJpa(order);
        ServiceOrderModel saved = jpaRepository.save(entity);
        return ServiceOrderJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<String> findLastOrderNumberByYearMonth(String yearMonth) {
        return jpaRepository.findLastOrderNumberByYearMonth(yearMonth);
    }

    @Override
    public Optional<ServiceOrder> findById(UUID id) {
        return jpaRepository.findById(id).map(ServiceOrderJpaMapper::toDomain);
    }

    @Override
    public List<ServiceOrder> findAll() {
        return jpaRepository.findAll().stream()
                .map(ServiceOrderJpaMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<ServiceOrder> findAllByOrderByCreatedAtDesc() {
        return jpaRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ServiceOrderJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ServiceOrder> findByOrderNumber(String orderNumber) {
        return jpaRepository.findByOrderNumber(orderNumber)
                .map(ServiceOrderJpaMapper::toDomain);
    }

    @Override
    public List<ServiceOrder> findActiveOrdersSortedByPriority() {
        return jpaRepository.findActiveOrdersSortedByPriority()
                .stream()
                .map(ServiceOrderJpaMapper::toDomain)
                .toList();
    }
}
