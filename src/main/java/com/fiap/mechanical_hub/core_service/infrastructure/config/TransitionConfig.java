package com.fiap.mechanical_hub.core_service.infrastructure.config;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class TransitionConfig {

    @Bean
    public OrderStatusTransitionFactory transitionFactory(
            ServiceOrderStatusHistoryGateway statusHistoryGateway, MeterRegistry registry) {
        return new OrderStatusTransitionFactory(
                Map.ofEntries(
                        Map.entry(OrderStatusEnum.EM_DIAGNOSTICO,
                                new StartDiagnosisTransition(statusHistoryGateway, registry)),

                        Map.entry(OrderStatusEnum.AGUARDANDO_APROVACAO,
                                new WaitingApprovalOrderTransition(statusHistoryGateway, registry)),

                        Map.entry(OrderStatusEnum.APROVADO,
                                new ApproveOrderTransition(statusHistoryGateway, registry)),

                        Map.entry(OrderStatusEnum.RECUSADO,
                                new RejectOrderTransition(statusHistoryGateway, registry)),

                        Map.entry(OrderStatusEnum.FALHA_PAGAMENTO,
                                new FailPaymentTransition(statusHistoryGateway, registry)),

                        Map.entry(OrderStatusEnum.PAGAMENTO_REALIZADO,
                                new ConfirmPaymentTransition(statusHistoryGateway, registry)),

                        Map.entry(OrderStatusEnum.PENDENTE_ESTOQUE,
                                new StockPendingTransition(statusHistoryGateway, registry)),

                        Map.entry(OrderStatusEnum.PRONTO_PARA_EXECUCAO,
                                new ReadyForExecutionTransition(statusHistoryGateway, registry)),

                        // Manual via PATCH /service-orders/{id}/status, a partir de
                        // PRONTO_PARA_EXECUCAO -- pagamento+material OK nao significa
                        // que o mecanico tem disponibilidade imediata (RFC-001).
                        Map.entry(OrderStatusEnum.EM_EXECUCAO,
                                new StartExecutionTransition(statusHistoryGateway, registry)),

                        Map.entry(OrderStatusEnum.FINALIZADO,
                                new FinishOrderTransition(statusHistoryGateway, registry)),

                        Map.entry(OrderStatusEnum.ENTREGUE,
                                new DeliverOrderTransition(statusHistoryGateway, registry))
                )
        );
    }
}
