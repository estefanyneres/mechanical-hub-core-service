package com.fiap.mechanical_hub.core_service.application.controllers;

import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.CreateServiceOrderRequest;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderDetailResponse;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderResponse;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderStatusHistoryResponse;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderSummaryResponse;
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

import java.util.List;
import java.util.UUID;

public class ServiceOrderController {

    private final ServiceOrderUseCase useCase;

    public ServiceOrderController(
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

    public ServiceOrderResponse create(CreateServiceOrderRequest request, UUID userId) {
        return useCase.create(request, userId);
    }

    public void updateOrderStatus(UUID id, String orderStatus, UUID userId) {
        useCase.updateOrderStatus(id, orderStatus, userId);
    }

    public List<ServiceOrderSummaryResponse> findAll() {
        return useCase.findAll();
    }

    public List<ServiceOrderSummaryResponse> findActiveSortedByPriority() {
        return useCase.findActiveSortedByPriority();
    }

    public ServiceOrderDetailResponse findById(UUID id) {
        return useCase.findById(id);
    }

    public String findStatusById(UUID id) {
        return useCase.findStatusById(id);
    }

    public List<ServiceOrderStatusHistoryResponse> findHistoryById(UUID id) {
        return useCase.findHistoryById(id);
    }
}
