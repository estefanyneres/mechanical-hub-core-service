package com.fiap.mechanical_hub.core_service.application.controllers;

import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderCustomerView;
import com.fiap.mechanical_hub.core_service.application.gateways.CustomerGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.VehicleGateway;
import com.fiap.mechanical_hub.core_service.application.mappers.CustomerMapper;
import com.fiap.mechanical_hub.core_service.application.mappers.VehicleMapper;
import com.fiap.mechanical_hub.core_service.application.presenters.CustomerPresenter;
import com.fiap.mechanical_hub.core_service.application.presenters.ServiceOrderPresenter;
import com.fiap.mechanical_hub.core_service.application.presenters.VehiclePresenter;
import com.fiap.mechanical_hub.core_service.application.usecases.CustomerUseCase;
import com.fiap.mechanical_hub.core_service.application.usecases.OrderNumberGenerator;
import com.fiap.mechanical_hub.core_service.application.usecases.SagaOrchestratorUseCase;
import com.fiap.mechanical_hub.core_service.application.usecases.ServiceOrderUseCase;
import com.fiap.mechanical_hub.core_service.application.usecases.VehicleUseCase;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.OrderStatusTransitionFactory;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.UUID;

/**
 * Reduzido pra Fase 4: approve/reject por cliente saíram (agora e o
 * billing-service que recebe a aprovacao/recusa do orcamento e publica
 * orcamento.aprovado/recusado - o SagaOrchestratorUseCase reage a isso,
 * nao um endpoint aqui). Sobra so a consulta por numero da OS.
 */
public class ExternalController {

    private final ServiceOrderUseCase useCase;

    public ExternalController(
            ServiceOrderGateway serviceOrderGateway,
            CustomerGateway customerGateway, CustomerMapper customerMapper, CustomerPresenter customerPresenter,
            VehicleGateway vehicleGateway, VehicleMapper vehicleMapper, VehiclePresenter vehiclePresenter,
            OrderStatusTransitionFactory factory, ServiceOrderPresenter serviceOrderPresenter,
            SagaOrchestratorUseCase sagaOrchestratorUseCase,
            ServiceOrderStatusHistoryGateway statusHistoryGateway, MeterRegistry registry) {

        CustomerUseCase customerUseCase = new CustomerUseCase(customerGateway, customerMapper, customerPresenter);
        VehicleUseCase vehicleUseCase = new VehicleUseCase(vehicleGateway, customerGateway, vehicleMapper, vehiclePresenter);
        OrderNumberGenerator orderNumberGenerator = new OrderNumberGenerator(serviceOrderGateway);

        this.useCase = new ServiceOrderUseCase(serviceOrderGateway, customerUseCase, vehicleUseCase,
                orderNumberGenerator, factory, serviceOrderPresenter, sagaOrchestratorUseCase,
                statusHistoryGateway, registry);
    }

    public ServiceOrderCustomerView findByOrderNumber(String orderNumber, UUID customerId) {
        return useCase.findByOrderNumber(orderNumber, customerId);
    }
}
