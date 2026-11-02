package com.fiap.mechanical_hub.core_service.application.gateways;

import com.fiap.mechanical_hub.core_service.domain.entities.SagaState;

import java.util.Optional;
import java.util.UUID;

public interface SagaStateGateway {
    SagaState save(SagaState sagaState);
    Optional<SagaState> findByServiceOrderId(UUID serviceOrderId);
}
