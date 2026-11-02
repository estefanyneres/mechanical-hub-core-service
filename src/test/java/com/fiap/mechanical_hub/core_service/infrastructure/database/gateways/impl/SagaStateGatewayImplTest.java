package com.fiap.mechanical_hub.core_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.core_service.domain.entities.SagaState;
import com.fiap.mechanical_hub.core_service.domain.enums.SagaStepEnum;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.SagaStateModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.repositories.SagaStateJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SagaStateGatewayImplTest {

    @Mock
    private SagaStateJpaRepository jpaRepository;

    @InjectMocks
    private SagaStateGatewayImpl gateway;

    @Test
    void shouldSaveSagaState() {
        SagaState state = SagaState.start(UUID.randomUUID());
        SagaStateModel model = new SagaStateModel(state.getId(), state.getServiceOrderId(), state.getEtapaAtual(),
                state.getCreatedAt(), state.getUpdatedAt());
        when(jpaRepository.save(any(SagaStateModel.class))).thenReturn(model);

        SagaState result = gateway.save(state);

        assertEquals(state.getId(), result.getId());
        assertEquals(SagaStepEnum.AGUARDANDO_ORCAMENTO, result.getEtapaAtual());
    }

    @Test
    void shouldFindByServiceOrderId() {
        UUID serviceOrderId = UUID.randomUUID();
        SagaState state = SagaState.start(serviceOrderId);
        SagaStateModel model = new SagaStateModel(state.getId(), serviceOrderId, state.getEtapaAtual(),
                state.getCreatedAt(), state.getUpdatedAt());
        when(jpaRepository.findFirstByServiceOrderIdOrderByCreatedAtDesc(serviceOrderId)).thenReturn(Optional.of(model));

        Optional<SagaState> result = gateway.findByServiceOrderId(serviceOrderId);

        assertTrue(result.isPresent());
        assertEquals(serviceOrderId, result.get().getServiceOrderId());
    }

    @Test
    void shouldReturnEmptyWhenNoSagaStateExists() {
        UUID serviceOrderId = UUID.randomUUID();
        when(jpaRepository.findFirstByServiceOrderIdOrderByCreatedAtDesc(serviceOrderId)).thenReturn(Optional.empty());

        assertTrue(gateway.findByServiceOrderId(serviceOrderId).isEmpty());
    }
}
