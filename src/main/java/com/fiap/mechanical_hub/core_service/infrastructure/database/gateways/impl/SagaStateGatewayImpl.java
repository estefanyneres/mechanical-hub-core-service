package com.fiap.mechanical_hub.core_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.core_service.application.gateways.SagaStateGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.SagaState;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.SagaStateModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.repositories.SagaStateJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SagaStateGatewayImpl implements SagaStateGateway {

    private final SagaStateJpaRepository jpaRepository;

    @Override
    public SagaState save(SagaState sagaState) {
        SagaStateModel saved = jpaRepository.save(toJpa(sagaState));
        return toDomain(saved);
    }

    @Override
    public Optional<SagaState> findByServiceOrderId(UUID serviceOrderId) {
        return jpaRepository.findFirstByServiceOrderIdOrderByCreatedAtDesc(serviceOrderId).map(this::toDomain);
    }

    private SagaStateModel toJpa(SagaState state) {
        return new SagaStateModel(
                state.getId(),
                state.getServiceOrderId(),
                state.getEtapaAtual(),
                state.getCreatedAt(),
                state.getUpdatedAt()
        );
    }

    private SagaState toDomain(SagaStateModel model) {
        return new SagaState(
                model.getId(),
                model.getServiceOrderId(),
                model.getEtapaAtual(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }
}
