package com.fiap.mechanical_hub.core_service.domain.strategies.order_transition;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.UUID;

/**
 * Manual (REST), a partir de PRONTO_PARA_EXECUCAO. No monolito essa
 * transicao acontecia implicitamente dentro de ServiceOrder.startTask().
 * Redesenhada na Fase 4 (TASK-016, apos feedback do usuario): pagamento
 * confirmado + material disponivel nao significa que o mecanico tem
 * disponibilidade imediata pra comecar -- por isso continua sendo uma
 * acao manual do mecanico (mesmo endpoint generico de sempre), nao um
 * gatilho automatico do Saga.
 */
public class StartExecutionTransition implements OrderStatusTransition {

    private final ServiceOrderStatusHistoryGateway statusHistoryGateway;
    private final MeterRegistry registry;

    public StartExecutionTransition(ServiceOrderStatusHistoryGateway statusHistoryGateway, MeterRegistry registry) {
        this.statusHistoryGateway = statusHistoryGateway;
        this.registry = registry;
    }

    @Override
    public void execute(ServiceOrder order, UUID userId) {
        OrderStatusEnum fromStatus = order.getStatus();
        order.startExecution();
        ServiceOrderStatusTransitionRecorder.record(
                statusHistoryGateway, registry, order, fromStatus, order.getStatus(), userId);
    }
}
