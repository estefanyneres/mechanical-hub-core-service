package com.fiap.mechanical_hub.core_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrderStatusHistory;
import com.fiap.mechanical_hub.core_service.infrastructure.database.mappers.ServiceOrderStatusHistoryJpaMapper;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.ServiceOrderStatusHistoryModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.repositories.ServiceOrderStatusHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServiceOrderStatusHistoryGatewayImpl implements ServiceOrderStatusHistoryGateway {

    private final ServiceOrderStatusHistoryJpaRepository jpaRepository;

    @Override
    public ServiceOrderStatusHistory save(ServiceOrderStatusHistory history) {
        ServiceOrderStatusHistoryModel model = ServiceOrderStatusHistoryJpaMapper.toJpa(history);
        ServiceOrderStatusHistoryModel saved = jpaRepository.save(model);
        return ServiceOrderStatusHistoryJpaMapper.toDomain(saved);
    }

    @Override
    public List<ServiceOrderStatusHistory> findByServiceOrderId(UUID serviceOrderId) {
        return jpaRepository.findByServiceOrderIdOrderByChangedAtAsc(serviceOrderId).stream()
                .map(ServiceOrderStatusHistoryJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ServiceOrderStatusHistory> findLastByServiceOrderId(UUID serviceOrderId) {
        return jpaRepository.findFirstByServiceOrderIdOrderByChangedAtDesc(serviceOrderId)
                .map(ServiceOrderStatusHistoryJpaMapper::toDomain);
    }
}
