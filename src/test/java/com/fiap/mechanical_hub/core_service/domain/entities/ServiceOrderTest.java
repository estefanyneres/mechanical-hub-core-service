package com.fiap.mechanical_hub.core_service.domain.entities;

import com.fiap.mechanical_hub.core_service.domain.entities.constants.TestConstants;
import com.fiap.mechanical_hub.core_service.domain.entities.mocks.ServiceOrderMock;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import com.fiap.mechanical_hub.core_service.domain.exceptions.BusinessRuleException;
import com.fiap.mechanical_hub.core_service.domain.exceptions.InvalidOrderTransitionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.fiap.mechanical_hub.core_service.domain.entities.constants.TestConstants.DEFAULT_USER_ID;
import static org.junit.jupiter.api.Assertions.*;

class ServiceOrderTest {

    @Test
    void shouldCreateServiceOrderWithValidData() {
        ServiceOrder order = ServiceOrderMock.defaultServiceOrder();

        assertNotNull(order.getId());
        assertEquals(TestConstants.DEFAULT_VEHICLE_ID, order.getVehicleId());
        assertEquals(TestConstants.DEFAULT_CUSTOMER_ID, order.getCustomerId());
        assertEquals(TestConstants.DEFAULT_ORDER_NUMBER, order.getOrderNumber());
        assertEquals(TestConstants.DEFAULT_REQUEST_DESCRIPTION, order.getRequestDescription());
        assertEquals(DEFAULT_USER_ID, order.getCreatedByUserId());
        assertEquals(OrderStatusEnum.RECEBIDO, order.getStatus());
        assertNull(order.getBudget());
        assertNotNull(order.getCreatedAt());
    }

    @Test
    void shouldTransitionFromRecebidoToDiagnostico() {
        ServiceOrder order = ServiceOrderMock.defaultServiceOrder();

        order.startDiagnosis(DEFAULT_USER_ID);

        assertEquals(OrderStatusEnum.EM_DIAGNOSTICO, order.getStatus());
        assertNotNull(order.getOpenedAt());
        assertEquals(DEFAULT_USER_ID, order.getResponsibleUserId());
    }

    @Test
    void shouldTransitionFromDiagnosticoToAguardandoAprovacao() {
        ServiceOrder order = ServiceOrderMock.serviceOrderInDiagnosis();
        order.updateBudget(TestConstants.DEFAULT_ORDER_BUDGET);

        order.submitForApproval();

        assertEquals(OrderStatusEnum.AGUARDANDO_APROVACAO, order.getStatus());
    }

    @Test
    void shouldTransitionFromAguardandoAprovacaoToAprovado() {
        ServiceOrder order = ServiceOrderMock.serviceOrderAwaitingApproval();

        order.approve();

        assertEquals(OrderStatusEnum.APROVADO, order.getStatus());
    }

    @Test
    void shouldTransitionFromAguardandoAprovacaoToRecusado() {
        ServiceOrder order = ServiceOrderMock.serviceOrderAwaitingApproval();

        order.reject();

        assertEquals(OrderStatusEnum.RECUSADO, order.getStatus());
    }

    @Test
    void shouldTransitionFromAprovadoToFalhaPagamento() {
        ServiceOrder order = ServiceOrderMock.serviceOrderApproved();

        order.failPayment();

        assertEquals(OrderStatusEnum.FALHA_PAGAMENTO, order.getStatus());
    }

    @Test
    void shouldConfirmPaymentFromAprovado() {
        ServiceOrder order = ServiceOrderMock.serviceOrderApproved();

        order.confirmPayment();

        assertEquals(OrderStatusEnum.PAGAMENTO_REALIZADO, order.getStatus());
    }

    @Test
    void shouldConfirmPaymentFromFalhaPagamentoRetry() {
        ServiceOrder order = ServiceOrderMock.serviceOrderWithFailedPayment();

        order.confirmPayment();

        assertEquals(OrderStatusEnum.PAGAMENTO_REALIZADO, order.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenConfirmingPaymentFromInvalidStatus() {
        ServiceOrder order = ServiceOrderMock.defaultServiceOrder();

        assertThrows(InvalidOrderTransitionException.class, order::confirmPayment);
    }

    @Test
    void shouldMarkReadyForExecutionFromPagamentoRealizado() {
        ServiceOrder order = ServiceOrderMock.servicePaymentRealized();

        order.markReadyForExecution();

        assertEquals(OrderStatusEnum.PRONTO_PARA_EXECUCAO, order.getStatus());
    }

    @Test
    void shouldMarkStockPendingFromPagamentoRealizado() {
        ServiceOrder order = ServiceOrderMock.servicePaymentRealized();

        order.markStockPending();

        assertEquals(OrderStatusEnum.PENDENTE_ESTOQUE, order.getStatus());
    }

    @Test
    void shouldMarkReadyForExecutionFromPendenteEstoque() {
        ServiceOrder order = ServiceOrderMock.serviceOrderStockPending();

        order.markReadyForExecution();

        assertEquals(OrderStatusEnum.PRONTO_PARA_EXECUCAO, order.getStatus());
    }

    @Test
    void shouldStartExecutionManuallyFromProntoParaExecucao() {
        ServiceOrder order = ServiceOrderMock.serviceOrderReadyForExecution();

        order.startExecution();

        assertEquals(OrderStatusEnum.EM_EXECUCAO, order.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenStartingExecutionWithoutMaterialConfirmation() {
        ServiceOrder order = ServiceOrderMock.servicePaymentRealized();

        assertThrows(InvalidOrderTransitionException.class, order::startExecution);
    }

    @Test
    void shouldTransitionFromEmExecucaoToFinalizado() {
        ServiceOrder order = ServiceOrderMock.serviceOrderInExecution();

        order.finish();

        assertEquals(OrderStatusEnum.FINALIZADO, order.getStatus());
        assertNotNull(order.getCompletedAt());
    }

    @Test
    void shouldTransitionFromFinalizadoToEntregue() {
        ServiceOrder order = ServiceOrderMock.serviceOrderFinalized();

        order.deliver();

        assertEquals(OrderStatusEnum.ENTREGUE, order.getStatus());
        assertNotNull(order.getDeliveredAt());
    }

    @Test
    void shouldRevertToDiagnosisAfterExecutionFailure() {
        ServiceOrder order = ServiceOrderMock.serviceOrderInExecution();
        order.updateBudget(BigDecimal.valueOf(999));

        order.revertToDiagnosisAfterExecutionFailure();

        assertEquals(OrderStatusEnum.EM_DIAGNOSTICO, order.getStatus());
        assertNull(order.getBudget());
    }

    @Test
    void shouldThrowExceptionWhenRevertingToDiagnosisFromInvalidStatus() {
        ServiceOrder order = ServiceOrderMock.defaultServiceOrder();

        assertThrows(InvalidOrderTransitionException.class, order::revertToDiagnosisAfterExecutionFailure);
    }

    @Test
    void shouldThrowExceptionWhenStartingDiagnosisFromInvalidStatus() {
        ServiceOrder order = ServiceOrderMock.serviceOrderInDiagnosis();

        assertThrows(InvalidOrderTransitionException.class, () -> order.startDiagnosis(DEFAULT_USER_ID));
    }

    /**
     * TASK-039: estes dois testes exigiam BusinessRuleException sem
     * orcamento -- comportamento do monolito. Na Fase 4 e o oposto: e
     * exatamente o pedido de orcamento que acontece DEPOIS dessa transicao
     * (ServiceOrderUseCase.updateOrderStatus -> SagaOrchestratorUseCase.
     * requestBudget), entao o orcamento tem que estar ausente aqui pra
     * refletir o fluxo real -- descoberto rodando o cenario BDD fim-a-fim.
     */
    @Test
    void shouldSubmitForApprovalWithoutBudget() {
        ServiceOrder order = ServiceOrderMock.serviceOrderInDiagnosis();

        order.submitForApproval();

        assertEquals(OrderStatusEnum.AGUARDANDO_APROVACAO, order.getStatus());
    }

    @Test
    void shouldSubmitForApprovalWithZeroBudget() {
        ServiceOrder order = ServiceOrderMock.serviceOrderInDiagnosis();
        order.updateBudget(BigDecimal.ZERO);

        order.submitForApproval();

        assertEquals(OrderStatusEnum.AGUARDANDO_APROVACAO, order.getStatus());
    }

    @Test
    void shouldUpdateBudget() {
        ServiceOrder order = ServiceOrderMock.defaultServiceOrder();
        BigDecimal newBudget = BigDecimal.valueOf(1000.00);

        order.updateBudget(newBudget);

        assertEquals(newBudget, order.getBudget());
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithBlankDescription() {
        assertThrows(BusinessRuleException.class, () -> ServiceOrder.create(
                TestConstants.DEFAULT_VEHICLE_ID, TestConstants.DEFAULT_CUSTOMER_ID,
                TestConstants.DEFAULT_ORDER_NUMBER, "", DEFAULT_USER_ID));
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithNullDescription() {
        assertThrows(BusinessRuleException.class, () -> ServiceOrder.create(
                TestConstants.DEFAULT_VEHICLE_ID, TestConstants.DEFAULT_CUSTOMER_ID,
                TestConstants.DEFAULT_ORDER_NUMBER, null, DEFAULT_USER_ID));
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithDescriptionTooLong() {
        String longDescription = "a".repeat(256);

        assertThrows(BusinessRuleException.class, () -> ServiceOrder.create(
                TestConstants.DEFAULT_VEHICLE_ID, TestConstants.DEFAULT_CUSTOMER_ID,
                TestConstants.DEFAULT_ORDER_NUMBER, longDescription, DEFAULT_USER_ID));
    }

    @Test
    void shouldHaveAllGettersPopulatedForFullLifecycleOrder() {
        ServiceOrder order = ServiceOrderMock.serviceOrderFinalized();
        order.deliver();

        assertNotNull(order.getOpenedAt());
        assertNotNull(order.getCompletedAt());
        assertNotNull(order.getDeliveredAt());
        assertNotNull(order.getUpdatedAt());
    }
}
