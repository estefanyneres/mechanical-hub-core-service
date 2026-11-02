package com.fiap.mechanical_hub.core_service.application.usecases;

import com.fiap.mechanical_hub.core_service.application.gateways.SagaStateGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.SagaState;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import com.fiap.mechanical_hub.core_service.domain.enums.SagaStepEnum;
import com.fiap.mechanical_hub.core_service.domain.exceptions.NotFoundException;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.OrderStatusTransitionFactory;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.RevertToDiagnosisTransition;
import com.fiap.mechanical_hub.core_service.infrastructure.messaging.SagaEventPublisher;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Orquestrador do Saga (RFC-001): embutido no core-service, estado
 * explicito em SagaState. Cada metodo publico corresponde a um evento
 * recebido via RabbitMQ (SagaEventListener) ou ao gatilho inicial do
 * fluxo distribuido.
 *
 * O Saga NAO comeca na criacao da OS -- comeca em requestBudget(), chamado
 * pelo ServiceOrderUseCase so quando o mecanico manualmente move a OS pra
 * AGUARDANDO_APROVACAO (mesma acao manual que ja existia no monolito pra
 * esse status). Isso resolve de raiz o race condition que existiria entre
 * esse pedido e a resposta do billing-service: a OS so entra no Saga
 * depois de ja estar em AGUARDANDO_APROVACAO, nunca antes.
 */
@Slf4j
public class SagaOrchestratorUseCase {

    private final SagaStateGateway sagaStateGateway;
    private final ServiceOrderGateway serviceOrderGateway;
    private final OrderStatusTransitionFactory factory;
    private final SagaEventPublisher publisher;
    private final RevertToDiagnosisTransition revertToDiagnosisTransition;

    public SagaOrchestratorUseCase(SagaStateGateway sagaStateGateway, ServiceOrderGateway serviceOrderGateway,
                                    OrderStatusTransitionFactory factory, SagaEventPublisher publisher,
                                    RevertToDiagnosisTransition revertToDiagnosisTransition) {
        this.sagaStateGateway = sagaStateGateway;
        this.serviceOrderGateway = serviceOrderGateway;
        this.factory = factory;
        this.publisher = publisher;
        this.revertToDiagnosisTransition = revertToDiagnosisTransition;
    }

    public void requestBudget(ServiceOrder order) {
        SagaState sagaState = SagaState.start(order.getId());
        sagaStateGateway.save(sagaState);
        publisher.publishOrcamentoSolicitado(order);
        log.info("Saga iniciado pra OS {} (etapa {}) - orcamento solicitado", order.getOrderNumber(), sagaState.getEtapaAtual());
    }

    public void onOrcamentoGerado(UUID serviceOrderId, BigDecimal valor) {
        ServiceOrder order = findOrder(serviceOrderId);
        order.updateBudget(valor);
        serviceOrderGateway.save(order);
        advanceSaga(serviceOrderId, SagaStepEnum.AGUARDANDO_APROVACAO_ORCAMENTO);
    }

    public void onOrcamentoAprovado(UUID serviceOrderId) {
        ServiceOrder order = findOrder(serviceOrderId);
        factory.get(OrderStatusEnum.APROVADO).execute(order, null);
        serviceOrderGateway.save(order);
        advanceSaga(serviceOrderId, SagaStepEnum.AGUARDANDO_PAGAMENTO);
    }

    public void onOrcamentoRecusado(UUID serviceOrderId, String motivo) {
        ServiceOrder order = findOrder(serviceOrderId);
        factory.get(OrderStatusEnum.RECUSADO).execute(order, null);
        serviceOrderGateway.save(order);
        advanceSaga(serviceOrderId, SagaStepEnum.FALHOU);
        log.info("OS {} recusada: {}", order.getOrderNumber(), motivo);
    }

    public void onPagamentoConfirmado(UUID serviceOrderId) {
        // So confirma o pagamento em si (APROVADO|FALHA_PAGAMENTO ->
        // PAGAMENTO_REALIZADO) - NAO avanca pra execucao direto. Material
        // disponivel e disponibilidade do mecanico sao fatos separados,
        // que chegam depois via material.reservado/indisponivel e via
        // acao manual, respectivamente.
        ServiceOrder order = findOrder(serviceOrderId);
        factory.get(OrderStatusEnum.PAGAMENTO_REALIZADO).execute(order, null);
        serviceOrderGateway.save(order);
        advanceSaga(serviceOrderId, SagaStepEnum.AGUARDANDO_EXECUCAO);
    }

    public void onPagamentoFalhou(UUID serviceOrderId, String motivo) {
        ServiceOrder order = findOrder(serviceOrderId);
        factory.get(OrderStatusEnum.FALHA_PAGAMENTO).execute(order, null);
        serviceOrderGateway.save(order);
        // Continua em AGUARDANDO_PAGAMENTO (nao FALHOU): o cliente pode
        // tentar pagar de novo: ConfirmPaymentTransition aceita
        // FALHA_PAGAMENTO como origem valida tambem.
        advanceSaga(serviceOrderId, SagaStepEnum.AGUARDANDO_PAGAMENTO);
        log.warn("Pagamento falhou pra OS {}: {}", serviceOrderId, motivo);
    }

    // Unico evento que avanca a OS pra PRONTO_PARA_EXECUCAO - publicado pelo
    // repair-service tanto na primeira tentativa (apos pagamento.confirmado)
    // quanto numa nova tentativa apos estoque.reabastecido. O core-service
    // nao reage a estoque.reabastecido diretamente: so confia numa reserva
    // de material confirmada de verdade (ServiceOrder.markReadyForExecution()
    // ja aceita PAGAMENTO_REALIZADO ou PENDENTE_ESTOQUE como origem).
    public void onMaterialReservado(UUID serviceOrderId) {
        ServiceOrder order = findOrder(serviceOrderId);
        factory.get(OrderStatusEnum.PRONTO_PARA_EXECUCAO).execute(order, null);
        serviceOrderGateway.save(order);
        advanceSaga(serviceOrderId, SagaStepEnum.AGUARDANDO_EXECUCAO);
    }

    public void onMaterialIndisponivel(UUID serviceOrderId, String motivo) {
        ServiceOrder order = findOrder(serviceOrderId);
        factory.get(OrderStatusEnum.PENDENTE_ESTOQUE).execute(order, null);
        serviceOrderGateway.save(order);
        // Continua em AGUARDANDO_EXECUCAO (saga ainda nao terminou, so
        // esta bloqueada esperando o estoque repor).
        advanceSaga(serviceOrderId, SagaStepEnum.AGUARDANDO_EXECUCAO);
        log.warn("Material indisponível pra OS {}: {}", serviceOrderId, motivo);
    }

    public void onExecucaoFinalizada(UUID serviceOrderId) {
        ServiceOrder order = findOrder(serviceOrderId);
        factory.get(OrderStatusEnum.FINALIZADO).execute(order, null);
        serviceOrderGateway.save(order);
        advanceSaga(serviceOrderId, SagaStepEnum.CONCLUIDO);
    }

    public void onExecucaoFalhou(UUID serviceOrderId, String motivo) {
        // Volta pra EM_DIAGNOSTICO pra reavaliacao do zero (decisao do
        // usuario - ver RFC-001). SagaState desta tentativa fica marcado
        // FALHOU (historico); a proxima vez que a OS passar por
        // AGUARDANDO_APROVACAO, requestBudget() cria uma nova linha de
        // SagaState (nao reaproveita esta) - cada tentativa e seu proprio
        // registro.
        ServiceOrder order = findOrder(serviceOrderId);
        revertToDiagnosisTransition.execute(order, null);
        serviceOrderGateway.save(order);
        advanceSaga(serviceOrderId, SagaStepEnum.FALHOU);
        publisher.publishExecucaoRevertida(serviceOrderId);
        log.warn("Execução falhou pra OS {}: {} - voltou pra EM_DIAGNOSTICO, material reservado liberado", serviceOrderId, motivo);
    }

    private ServiceOrder findOrder(UUID serviceOrderId) {
        return serviceOrderGateway.findById(serviceOrderId)
                .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada com ID: " + serviceOrderId));
    }

    private void advanceSaga(UUID serviceOrderId, SagaStepEnum step) {
        SagaState sagaState = sagaStateGateway.findByServiceOrderId(serviceOrderId)
                .orElseThrow(() -> new NotFoundException("Saga não encontrado pra OS: " + serviceOrderId));
        sagaState.advanceTo(step);
        sagaStateGateway.save(sagaState);
    }
}
