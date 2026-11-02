package com.fiap.mechanical_hub.core_service.bdd.fakes;

import com.fiap.mechanical_hub.core_service.application.gateways.SagaStateGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.SagaState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemorySagaStateGateway implements SagaStateGateway {

    private final Map<UUID, SagaState> storeByServiceOrderId = new LinkedHashMap<>();

    @Override
    public SagaState save(SagaState sagaState) {
        storeByServiceOrderId.put(sagaState.getServiceOrderId(), sagaState);
        return sagaState;
    }

    @Override
    public Optional<SagaState> findByServiceOrderId(UUID serviceOrderId) {
        return Optional.ofNullable(storeByServiceOrderId.get(serviceOrderId));
    }
}
