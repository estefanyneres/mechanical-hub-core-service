package com.fiap.mechanical_hub.core_service.domain.entities;

import com.fiap.mechanical_hub.core_service.domain.enums.SagaStepEnum;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Estado do Saga orquestrado (RFC-001). Uma linha por ServiceOrder, dono
 * da etapa atual do fluxo abrir OS -> orcamento -> aprovacao -> pagamento
 * -> execucao. Separado do OrderStatusEnum de proposito: etapaAtual e
 * estado de PROCESSO (onde estamos no fluxo distribuido), status e estado
 * de NEGOCIO (o que o cliente/mecanico ve).
 */
public class SagaState {

    private UUID id;
    private UUID serviceOrderId;
    private SagaStepEnum etapaAtual;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SagaState() {}

    public SagaState(UUID id, UUID serviceOrderId, SagaStepEnum etapaAtual,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.serviceOrderId = serviceOrderId;
        this.etapaAtual = etapaAtual;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SagaState start(UUID serviceOrderId) {
        SagaState state = new SagaState();
        state.id = UUID.randomUUID();
        state.serviceOrderId = serviceOrderId;
        state.etapaAtual = SagaStepEnum.AGUARDANDO_ORCAMENTO;
        state.createdAt = LocalDateTime.now();
        state.updatedAt = LocalDateTime.now();
        return state;
    }

    public void advanceTo(SagaStepEnum step) {
        this.etapaAtual = step;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getServiceOrderId() { return serviceOrderId; }
    public SagaStepEnum getEtapaAtual() { return etapaAtual; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
