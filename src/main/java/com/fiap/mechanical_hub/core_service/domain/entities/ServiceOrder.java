package com.fiap.mechanical_hub.core_service.domain.entities;

import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import com.fiap.mechanical_hub.core_service.domain.exceptions.BusinessRuleException;
import com.fiap.mechanical_hub.core_service.domain.exceptions.InvalidOrderTransitionException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Redesenhada pra Fase 4 (TASK-015): sem a lista de OrderTask (agora
 * repair-service) e sem hasStockPending (reserva de estoque ficou
 * assincrona/pos-pagamento - RFC-001). finish() nao depende mais de
 * checar tarefas localmente: quem dispara essa transicao e o
 * SagaOrchestratorUseCase ao receber o evento execucao.finalizada.
 */
public class ServiceOrder {

    private UUID id;
    private UUID vehicleId;
    private UUID customerId;
    private OrderStatusEnum status;
    private UUID createdByUserId;
    private UUID responsibleUserId;
    private String orderNumber;
    private String requestDescription;
    private BigDecimal budget;
    private LocalDateTime estimatedCompletionAt;
    private LocalDateTime openedAt;
    private LocalDateTime completedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ServiceOrder() {}

    public ServiceOrder(UUID id, UUID vehicleId, UUID customerId, OrderStatusEnum status,
                        UUID createdByUserId, UUID responsibleUserId, String orderNumber,
                        String requestDescription, BigDecimal budget,
                        LocalDateTime estimatedCompletionAt, LocalDateTime openedAt,
                        LocalDateTime completedAt, LocalDateTime deliveredAt,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.status = status;
        this.createdByUserId = createdByUserId;
        this.responsibleUserId = responsibleUserId;
        this.orderNumber = orderNumber;
        this.requestDescription = requestDescription;
        this.budget = budget;
        this.estimatedCompletionAt = estimatedCompletionAt;
        this.openedAt = openedAt;
        this.completedAt = completedAt;
        this.deliveredAt = deliveredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ServiceOrder create(UUID vehicleId, UUID customerId, String orderNumber,
                                      String requestDescription, UUID createdByUserId) {
        if (requestDescription == null || requestDescription.isBlank()) {
            throw new BusinessRuleException("A descrição da solicitação é obrigatória");
        }
        if (requestDescription.length() > 255) {
            throw new BusinessRuleException("A descrição da solicitação não pode ultrapassar 255 caracteres");
        }

        ServiceOrder order = new ServiceOrder();
        order.id = UUID.randomUUID();
        order.vehicleId = vehicleId;
        order.customerId = customerId;
        order.status = OrderStatusEnum.RECEBIDO;
        order.createdByUserId = createdByUserId;
        order.responsibleUserId = null;
        order.orderNumber = orderNumber;
        order.requestDescription = requestDescription;
        order.budget = null;
        order.estimatedCompletionAt = null;
        order.openedAt = null;
        order.completedAt = null;
        order.deliveredAt = null;
        order.createdAt = LocalDateTime.now();
        order.updatedAt = LocalDateTime.now();
        return order;
    }

    public void startDiagnosis(UUID userId) {
        validateCurrentStatus(OrderStatusEnum.RECEBIDO);

        this.responsibleUserId = userId;
        this.status = OrderStatusEnum.EM_DIAGNOSTICO;
        this.openedAt = LocalDateTime.now();
    }

    /**
     * TASK-039 (achado durante o teste BDD fim-a-fim): removida a checagem
     * de orcamento ja existente, que era do monolito. No desenho da Fase 4
     * isso ficou invertido -- e o PROPRIO ato de entrar em
     * AGUARDANDO_APROVACAO que dispara o pedido de orcamento
     * (ServiceOrderUseCase.updateOrderStatus -> SagaOrchestratorUseCase.
     * requestBudget), entao o orcamento NUNCA existe ainda nesse momento.
     * Manter a checagem antiga quebrava toda tentativa real de enviar uma
     * OS pra aprovacao.
     */
    public void submitForApproval() {
        validateCurrentStatus(OrderStatusEnum.EM_DIAGNOSTICO);

        this.status = OrderStatusEnum.AGUARDANDO_APROVACAO;
    }

    public void approve() {
        validateCurrentStatus(OrderStatusEnum.AGUARDANDO_APROVACAO);

        this.status = OrderStatusEnum.APROVADO;
    }

    public void reject() {
        validateCurrentStatus(OrderStatusEnum.AGUARDANDO_APROVACAO);

        this.status = OrderStatusEnum.RECUSADO;
    }

    public void failPayment() {
        validateCurrentStatus(OrderStatusEnum.APROVADO);

        this.status = OrderStatusEnum.FALHA_PAGAMENTO;
    }

    public void confirmPayment() {
        validateCurrentStatusIn(OrderStatusEnum.APROVADO, OrderStatusEnum.FALHA_PAGAMENTO);

        this.status = OrderStatusEnum.PAGAMENTO_REALIZADO;
    }

    public void markStockPending() {
        validateCurrentStatus(OrderStatusEnum.PAGAMENTO_REALIZADO);

        this.status = OrderStatusEnum.PENDENTE_ESTOQUE;
    }

    public void markReadyForExecution() {
        validateCurrentStatusIn(OrderStatusEnum.PAGAMENTO_REALIZADO, OrderStatusEnum.PENDENTE_ESTOQUE);

        this.status = OrderStatusEnum.PRONTO_PARA_EXECUCAO;
    }

    public void startExecution() {
        validateCurrentStatus(OrderStatusEnum.PRONTO_PARA_EXECUCAO);

        this.status = OrderStatusEnum.EM_EXECUCAO;
    }

    public void revertToDiagnosisAfterExecutionFailure() {
        validateCurrentStatus(OrderStatusEnum.EM_EXECUCAO);

        this.status = OrderStatusEnum.EM_DIAGNOSTICO;
        // Reavaliada do zero: o orcamento antigo nao vale mais, o proximo
        // ciclo de aprovacao vai gerar um orcamento.gerado novo.
        this.budget = null;
    }

    public void finish() {
        validateCurrentStatus(OrderStatusEnum.EM_EXECUCAO);

        this.status = OrderStatusEnum.FINALIZADO;
        this.completedAt = LocalDateTime.now();
    }

    public void deliver() {
        validateCurrentStatus(OrderStatusEnum.FINALIZADO);

        this.status = OrderStatusEnum.ENTREGUE;
        this.deliveredAt = LocalDateTime.now();
    }

    private void validateCurrentStatus(OrderStatusEnum expected) {
        if (status != expected) {
            throw new InvalidOrderTransitionException("Transição inválida a partir do status: " + status.getDisplayName());
        }
    }

    private void validateCurrentStatusIn(OrderStatusEnum... allowed) {
        for (OrderStatusEnum status : allowed) {
            if (this.status == status) { return; }
        }
        throw new InvalidOrderTransitionException("Transição inválida a partir do status: " + status.getDisplayName());
    }

    public void updateBudget(BigDecimal newBudget) {
        this.budget = newBudget;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getVehicleId() { return vehicleId; }
    public UUID getCustomerId() { return customerId; }
    public OrderStatusEnum getStatus() { return status; }
    public UUID getCreatedByUserId() { return createdByUserId; }
    public UUID getResponsibleUserId() { return responsibleUserId; }
    public String getOrderNumber() { return orderNumber; }
    public String getRequestDescription() { return requestDescription; }
    public BigDecimal getBudget() { return budget; }
    public LocalDateTime getEstimatedCompletionAt() { return estimatedCompletionAt; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
