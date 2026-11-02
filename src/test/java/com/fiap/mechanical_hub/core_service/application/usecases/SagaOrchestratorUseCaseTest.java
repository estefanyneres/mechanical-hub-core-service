package com.fiap.mechanical_hub.core_service.application.usecases;

import com.fiap.mechanical_hub.core_service.application.gateways.SagaStateGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.SagaState;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import com.fiap.mechanical_hub.core_service.domain.enums.SagaStepEnum;
import com.fiap.mechanical_hub.core_service.domain.exceptions.NotFoundException;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.OrderStatusTransition;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.OrderStatusTransitionFactory;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.RevertToDiagnosisTransition;
import com.fiap.mechanical_hub.core_service.infrastructure.messaging.SagaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SagaOrchestratorUseCaseTest {

    @Mock private SagaStateGateway sagaStateGateway;
    @Mock private ServiceOrderGateway serviceOrderGateway;
    @Mock private OrderStatusTransitionFactory factory;
    @Mock private SagaEventPublisher publisher;
    @Mock private RevertToDiagnosisTransition revertToDiagnosisTransition;
    @Mock private OrderStatusTransition transition;

    private SagaOrchestratorUseCase orchestrator;

    private final UUID orderId = UUID.randomUUID();
    private ServiceOrder order;
    private SagaState sagaState;

    @BeforeEach
    void setUp() {
        orchestrator = new SagaOrchestratorUseCase(
                sagaStateGateway, serviceOrderGateway, factory, publisher, revertToDiagnosisTransition);
        order = ServiceOrder.create(UUID.randomUUID(), UUID.randomUUID(), "OS-202601-0001", "desc", UUID.randomUUID());
        sagaState = SagaState.start(orderId);
        lenient().when(serviceOrderGateway.findById(orderId)).thenReturn(Optional.of(order));
        lenient().when(serviceOrderGateway.save(any(ServiceOrder.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void shouldRequestBudgetAndPublishEvent() {
        orchestrator.requestBudget(order);

        verify(sagaStateGateway).save(any(SagaState.class));
        verify(publisher).publishOrcamentoSolicitado(order);
    }

    @Test
    void shouldUpdateBudgetOnOrcamentoGerado() {
        when(sagaStateGateway.findByServiceOrderId(orderId)).thenReturn(Optional.of(sagaState));

        orchestrator.onOrcamentoGerado(orderId, BigDecimal.valueOf(500));

        assertEquals(BigDecimal.valueOf(500), order.getBudget());
        ArgumentCaptor<SagaState> captor = ArgumentCaptor.forClass(SagaState.class);
        verify(sagaStateGateway).save(captor.capture());
        assertEquals(SagaStepEnum.AGUARDANDO_APROVACAO_ORCAMENTO, captor.getValue().getEtapaAtual());
    }

    @Test
    void shouldApproveOrderOnOrcamentoAprovado() {
        when(factory.get(OrderStatusEnum.APROVADO)).thenReturn(transition);
        when(sagaStateGateway.findByServiceOrderId(orderId)).thenReturn(Optional.of(sagaState));

        orchestrator.onOrcamentoAprovado(orderId);

        verify(transition).execute(order, null);
        assertEquals(SagaStepEnum.AGUARDANDO_PAGAMENTO, sagaState.getEtapaAtual());
    }

    @Test
    void shouldRejectOrderOnOrcamentoRecusado() {
        when(factory.get(OrderStatusEnum.RECUSADO)).thenReturn(transition);
        when(sagaStateGateway.findByServiceOrderId(orderId)).thenReturn(Optional.of(sagaState));

        orchestrator.onOrcamentoRecusado(orderId, "sem orçamento aprovado");

        verify(transition).execute(order, null);
        assertEquals(SagaStepEnum.FALHOU, sagaState.getEtapaAtual());
    }

    @Test
    void shouldConfirmPaymentOnPagamentoConfirmado() {
        when(factory.get(OrderStatusEnum.PAGAMENTO_REALIZADO)).thenReturn(transition);
        when(sagaStateGateway.findByServiceOrderId(orderId)).thenReturn(Optional.of(sagaState));

        orchestrator.onPagamentoConfirmado(orderId);

        verify(transition).execute(order, null);
        assertEquals(SagaStepEnum.AGUARDANDO_EXECUCAO, sagaState.getEtapaAtual());
    }

    @Test
    void shouldFailPaymentAndStayAwaitingPaymentOnPagamentoFalhou() {
        when(factory.get(OrderStatusEnum.FALHA_PAGAMENTO)).thenReturn(transition);
        when(sagaStateGateway.findByServiceOrderId(orderId)).thenReturn(Optional.of(sagaState));

        orchestrator.onPagamentoFalhou(orderId, "cartão recusado");

        verify(transition).execute(order, null);
        assertEquals(SagaStepEnum.AGUARDANDO_PAGAMENTO, sagaState.getEtapaAtual());
    }

    @Test
    void shouldMarkReadyForExecutionOnMaterialReservado() {
        when(factory.get(OrderStatusEnum.PRONTO_PARA_EXECUCAO)).thenReturn(transition);
        when(sagaStateGateway.findByServiceOrderId(orderId)).thenReturn(Optional.of(sagaState));

        orchestrator.onMaterialReservado(orderId);

        verify(transition).execute(order, null);
        assertEquals(SagaStepEnum.AGUARDANDO_EXECUCAO, sagaState.getEtapaAtual());
    }

    @Test
    void shouldMarkStockPendingOnMaterialIndisponivel() {
        when(factory.get(OrderStatusEnum.PENDENTE_ESTOQUE)).thenReturn(transition);
        when(sagaStateGateway.findByServiceOrderId(orderId)).thenReturn(Optional.of(sagaState));

        orchestrator.onMaterialIndisponivel(orderId, "sem peça em estoque");

        verify(transition).execute(order, null);
        assertEquals(SagaStepEnum.AGUARDANDO_EXECUCAO, sagaState.getEtapaAtual());
    }

    @Test
    void shouldFinishOrderOnExecucaoFinalizada() {
        when(factory.get(OrderStatusEnum.FINALIZADO)).thenReturn(transition);
        when(sagaStateGateway.findByServiceOrderId(orderId)).thenReturn(Optional.of(sagaState));

        orchestrator.onExecucaoFinalizada(orderId);

        verify(transition).execute(order, null);
        assertEquals(SagaStepEnum.CONCLUIDO, sagaState.getEtapaAtual());
    }

    @Test
    void shouldRevertToDiagnosisOnExecucaoFalhou() {
        when(sagaStateGateway.findByServiceOrderId(orderId)).thenReturn(Optional.of(sagaState));

        orchestrator.onExecucaoFalhou(orderId, "peça quebrou durante o reparo");

        verify(revertToDiagnosisTransition).execute(order, null);
        verify(publisher).publishExecucaoRevertida(orderId);
        verifyNoInteractions(factory);
        assertEquals(SagaStepEnum.FALHOU, sagaState.getEtapaAtual());
    }

    @Test
    void shouldThrowNotFoundWhenOrderDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(serviceOrderGateway.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orchestrator.onOrcamentoGerado(unknownId, BigDecimal.TEN));
    }

    @Test
    void shouldThrowNotFoundWhenSagaStateDoesNotExist() {
        when(factory.get(OrderStatusEnum.APROVADO)).thenReturn(transition);
        when(sagaStateGateway.findByServiceOrderId(orderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orchestrator.onOrcamentoAprovado(orderId));
    }
}
