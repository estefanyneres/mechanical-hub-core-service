package com.fiap.mechanical_hub.core_service.domain.strategies.order_transition;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.UUID;

/**
 * Disparada pelo SagaOrchestratorUseCase ao receber execucao.falhou
 * (falha durante a execucao manual ja iniciada): volta a OS pra
 * EM_DIAGNOSTICO pra reavaliacao do zero. Nao entra no mapa de
 * OrderStatusTransitionFactory porque EM_DIAGNOSTICO ja e o alvo de
 * StartDiagnosisTransition (RECEBIDO -> EM_DIAGNOSTICO, manual) -- o
 * mesmo status de destino, alcancado por dois caminhos com efeitos
 * diferentes, nao cabe numa unica entrada de mapa chaveada por status
 * destino. Por isso e injetada direto no SagaOrchestratorUseCase (ver
 * SagaConfig), nao atraves da factory.
 */
public class RevertToDiagnosisTransition implements OrderStatusTransition {

    private final ServiceOrderStatusHistoryGateway statusHistoryGateway;
    private final MeterRegistry registry;

    public RevertToDiagnosisTransition(ServiceOrderStatusHistoryGateway statusHistoryGateway, MeterRegistry registry) {
        this.statusHistoryGateway = statusHistoryGateway;
        this.registry = registry;
    }

    @Override
    public void execute(ServiceOrder order, UUID userId) {
        OrderStatusEnum fromStatus = order.getStatus();
        order.revertToDiagnosisAfterExecutionFailure();
        ServiceOrderStatusTransitionRecorder.record(
                statusHistoryGateway, registry, order, fromStatus, order.getStatus(), userId);
    }
}
