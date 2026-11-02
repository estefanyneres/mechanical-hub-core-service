package com.fiap.mechanical_hub.core_service.bdd;

import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.CreateServiceOrderRequest;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.CustomerData;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderResponse;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.VehicleData;
import com.fiap.mechanical_hub.core_service.application.usecases.CustomerUseCase;
import com.fiap.mechanical_hub.core_service.application.usecases.OrderNumberGenerator;
import com.fiap.mechanical_hub.core_service.application.usecases.SagaOrchestratorUseCase;
import com.fiap.mechanical_hub.core_service.application.usecases.ServiceOrderUseCase;
import com.fiap.mechanical_hub.core_service.application.usecases.VehicleUseCase;
import com.fiap.mechanical_hub.core_service.bdd.fakes.InMemoryCustomerGateway;
import com.fiap.mechanical_hub.core_service.bdd.fakes.InMemorySagaStateGateway;
import com.fiap.mechanical_hub.core_service.bdd.fakes.InMemoryServiceOrderGateway;
import com.fiap.mechanical_hub.core_service.bdd.fakes.InMemoryServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.bdd.fakes.InMemoryVehicleGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import com.fiap.mechanical_hub.core_service.domain.enums.SagaStepEnum;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.ApproveOrderTransition;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.ConfirmPaymentTransition;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.DeliverOrderTransition;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.FailPaymentTransition;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.FinishOrderTransition;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.OrderStatusTransitionFactory;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.ReadyForExecutionTransition;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.RejectOrderTransition;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.RevertToDiagnosisTransition;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.StartDiagnosisTransition;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.StartExecutionTransition;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.StockPendingTransition;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.WaitingApprovalOrderTransition;
import com.fiap.mechanical_hub.core_service.infrastructure.http.presenters.ServiceOrderPresenterImpl;
import com.fiap.mechanical_hub.core_service.infrastructure.messaging.SagaEventPublisher;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * TASK-039: dirige codigo de producao real (ServiceOrderUseCase,
 * SagaOrchestratorUseCase, as transicoes reais, a entidade ServiceOrder
 * real) com gateways fake em memoria - mesma filosofia dos testes
 * unitarios do projeto (sem Spring, sem banco, sem RabbitMQ reais). So o
 * SagaEventPublisher (quem de verdade publicaria no RabbitMQ) e mockado,
 * pra poder verificar o que foi publicado sem precisar de um broker.
 * A chegada dos eventos externos (orcamento.gerado/aprovado,
 * pagamento.confirmado, material.reservado) e simulada chamando
 * diretamente o mesmo metodo que o SagaEventListener chamaria depois de
 * decodificar a mensagem real - a logica de orquestracao em si roda sem
 * nenhuma simulacao.
 */
public class SagaFluxoCompletoSteps {

    private final InMemoryServiceOrderGateway serviceOrderGateway = new InMemoryServiceOrderGateway();
    private final InMemoryCustomerGateway customerGateway = new InMemoryCustomerGateway();
    private final InMemoryVehicleGateway vehicleGateway = new InMemoryVehicleGateway();
    private final InMemoryServiceOrderStatusHistoryGateway statusHistoryGateway =
            new InMemoryServiceOrderStatusHistoryGateway();
    private final InMemorySagaStateGateway sagaStateGateway = new InMemorySagaStateGateway();
    private final SagaEventPublisher publisher = mock(SagaEventPublisher.class);
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

    private final CustomerUseCase customerUseCase = new CustomerUseCase(customerGateway, null, null);
    private final VehicleUseCase vehicleUseCase = new VehicleUseCase(vehicleGateway, customerGateway, null, null);
    private final OrderNumberGenerator orderNumberGenerator = new OrderNumberGenerator(serviceOrderGateway);

    // Mesmo mapa de transicoes que o TransitionConfig real monta via Spring -
    // aqui construido a mao, sem contexto, com as MESMAS classes de producao.
    private final OrderStatusTransitionFactory factory = new OrderStatusTransitionFactory(Map.ofEntries(
            Map.entry(OrderStatusEnum.EM_DIAGNOSTICO, new StartDiagnosisTransition(statusHistoryGateway, registry)),
            Map.entry(OrderStatusEnum.AGUARDANDO_APROVACAO, new WaitingApprovalOrderTransition(statusHistoryGateway, registry)),
            Map.entry(OrderStatusEnum.APROVADO, new ApproveOrderTransition(statusHistoryGateway, registry)),
            Map.entry(OrderStatusEnum.RECUSADO, new RejectOrderTransition(statusHistoryGateway, registry)),
            Map.entry(OrderStatusEnum.FALHA_PAGAMENTO, new FailPaymentTransition(statusHistoryGateway, registry)),
            Map.entry(OrderStatusEnum.PAGAMENTO_REALIZADO, new ConfirmPaymentTransition(statusHistoryGateway, registry)),
            Map.entry(OrderStatusEnum.PENDENTE_ESTOQUE, new StockPendingTransition(statusHistoryGateway, registry)),
            Map.entry(OrderStatusEnum.PRONTO_PARA_EXECUCAO, new ReadyForExecutionTransition(statusHistoryGateway, registry)),
            Map.entry(OrderStatusEnum.EM_EXECUCAO, new StartExecutionTransition(statusHistoryGateway, registry)),
            Map.entry(OrderStatusEnum.FINALIZADO, new FinishOrderTransition(statusHistoryGateway, registry)),
            Map.entry(OrderStatusEnum.ENTREGUE, new DeliverOrderTransition(statusHistoryGateway, registry))
    ));

    private final RevertToDiagnosisTransition revertToDiagnosisTransition =
            new RevertToDiagnosisTransition(statusHistoryGateway, registry);

    private final SagaOrchestratorUseCase sagaOrchestratorUseCase = new SagaOrchestratorUseCase(
            sagaStateGateway, serviceOrderGateway, factory, publisher, revertToDiagnosisTransition);

    private final ServiceOrderUseCase serviceOrderUseCase = new ServiceOrderUseCase(
            serviceOrderGateway, customerUseCase, vehicleUseCase, orderNumberGenerator, factory,
            new ServiceOrderPresenterImpl(), sagaOrchestratorUseCase, statusHistoryGateway, registry);

    private final UUID mecanicoId = UUID.randomUUID();
    private UUID serviceOrderId;

    @Dado("que uma ordem de serviço foi criada")
    public void que_uma_ordem_de_servico_foi_criada() {
        CreateServiceOrderRequest request = new CreateServiceOrderRequest(
                new CustomerData("Marcos Ferreira", "CPF", "52998224725", "+5511999999999",
                        "marcos@example.com", "Rua Um, 123"),
                new VehicleData("ABC1234", "Fiat", "Uno", 2015, "Branco"),
                "Revisão geral"
        );

        ServiceOrderResponse response = serviceOrderUseCase.create(request, mecanicoId);
        serviceOrderId = response.id();
    }

    @Quando("o mecânico inicia o diagnóstico")
    public void o_mecanico_inicia_o_diagnostico() {
        serviceOrderUseCase.updateOrderStatus(serviceOrderId, OrderStatusEnum.EM_DIAGNOSTICO.name(), mecanicoId);
    }

    @E("o mecânico envia a ordem para aprovação")
    public void o_mecanico_envia_a_ordem_para_aprovacao() {
        serviceOrderUseCase.updateOrderStatus(serviceOrderId, OrderStatusEnum.AGUARDANDO_APROVACAO.name(), mecanicoId);
    }

    @Então("o evento {string} deve ter sido publicado")
    public void o_evento_deve_ter_sido_publicado(String evento) {
        if ("orcamento.solicitado".equals(evento)) {
            verify(publisher).publishOrcamentoSolicitado(any(ServiceOrder.class));
        } else {
            throw new IllegalArgumentException("Evento não reconhecido pelo step: " + evento);
        }
    }

    @Quando("o orçamento é gerado no valor de {string}")
    public void o_orcamento_e_gerado_no_valor_de(String valor) {
        sagaOrchestratorUseCase.onOrcamentoGerado(serviceOrderId, new BigDecimal(valor));
    }

    @Então("a ordem deve estar com o orçamento atualizado para {string}")
    public void a_ordem_deve_estar_com_o_orcamento_atualizado_para(String valorEsperado) {
        ServiceOrder order = serviceOrderGateway.findById(serviceOrderId).orElseThrow();
        assertEquals(new BigDecimal(valorEsperado), order.getBudget());
    }

    @Quando("o cliente aprova o orçamento")
    public void o_cliente_aprova_o_orcamento() {
        sagaOrchestratorUseCase.onOrcamentoAprovado(serviceOrderId);
    }

    @Quando("o pagamento é confirmado")
    public void o_pagamento_e_confirmado() {
        sagaOrchestratorUseCase.onPagamentoConfirmado(serviceOrderId);
    }

    @Quando("o material é reservado com sucesso")
    public void o_material_e_reservado_com_sucesso() {
        sagaOrchestratorUseCase.onMaterialReservado(serviceOrderId);
    }

    @Então("o status da ordem deve ser {string}")
    public void o_status_da_ordem_deve_ser(String statusEsperado) {
        ServiceOrder order = serviceOrderGateway.findById(serviceOrderId).orElseThrow();
        assertEquals(OrderStatusEnum.valueOf(statusEsperado), order.getStatus());
    }

    @E("a etapa do saga deve ser {string}")
    public void a_etapa_do_saga_deve_ser(String etapaEsperada) {
        var sagaState = sagaStateGateway.findByServiceOrderId(serviceOrderId).orElseThrow();
        assertEquals(SagaStepEnum.valueOf(etapaEsperada), sagaState.getEtapaAtual());
    }
}
