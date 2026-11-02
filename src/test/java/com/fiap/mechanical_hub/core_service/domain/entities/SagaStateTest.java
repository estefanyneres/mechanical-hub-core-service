package com.fiap.mechanical_hub.core_service.domain.entities;

import com.fiap.mechanical_hub.core_service.domain.enums.SagaStepEnum;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SagaStateTest {

    @Test
    void shouldStartSagaStateWithAguardandoOrcamento() {
        UUID serviceOrderId = UUID.randomUUID();

        SagaState state = SagaState.start(serviceOrderId);

        assertNotNull(state.getId());
        assertEquals(serviceOrderId, state.getServiceOrderId());
        assertEquals(SagaStepEnum.AGUARDANDO_ORCAMENTO, state.getEtapaAtual());
        assertNotNull(state.getCreatedAt());
        assertNotNull(state.getUpdatedAt());
    }

    @Test
    void shouldAdvanceToNewStep() {
        SagaState state = SagaState.start(UUID.randomUUID());

        state.advanceTo(SagaStepEnum.AGUARDANDO_PAGAMENTO);

        assertEquals(SagaStepEnum.AGUARDANDO_PAGAMENTO, state.getEtapaAtual());
    }

    @Test
    void shouldCreateDifferentStatesWithDifferentIds() {
        UUID serviceOrderId = UUID.randomUUID();

        SagaState state1 = SagaState.start(serviceOrderId);
        SagaState state2 = SagaState.start(serviceOrderId);

        assertNotEquals(state1.getId(), state2.getId());
    }
}
