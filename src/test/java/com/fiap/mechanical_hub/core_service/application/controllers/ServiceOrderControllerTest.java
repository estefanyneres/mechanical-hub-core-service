package com.fiap.mechanical_hub.core_service.application.controllers;

import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.*;
import com.fiap.mechanical_hub.core_service.application.gateways.CustomerGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.VehicleGateway;
import com.fiap.mechanical_hub.core_service.application.mappers.CustomerMapper;
import com.fiap.mechanical_hub.core_service.application.mappers.VehicleMapper;
import com.fiap.mechanical_hub.core_service.application.presenters.CustomerPresenter;
import com.fiap.mechanical_hub.core_service.application.presenters.ServiceOrderPresenter;
import com.fiap.mechanical_hub.core_service.application.presenters.VehiclePresenter;
import com.fiap.mechanical_hub.core_service.application.usecases.SagaOrchestratorUseCase;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.OrderStatusTransitionFactory;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOrderControllerTest {

    @Mock private ServiceOrderGateway serviceOrderGateway;
    @Mock private CustomerGateway customerGateway;
    @Mock private CustomerMapper customerMapper;
    @Mock private CustomerPresenter customerPresenter;
    @Mock private VehicleGateway vehicleGateway;
    @Mock private VehicleMapper vehicleMapper;
    @Mock private VehiclePresenter vehiclePresenter;
    @Mock private OrderStatusTransitionFactory factory;
    @Mock private ServiceOrderPresenter serviceOrderPresenter;
    @Mock private SagaOrchestratorUseCase sagaOrchestratorUseCase;
    @Mock private ServiceOrderStatusHistoryGateway statusHistoryGateway;

    private ServiceOrderController controller;

    private ServiceOrderController build() {
        return new ServiceOrderController(serviceOrderGateway, customerGateway, customerMapper, customerPresenter,
                vehicleGateway, vehicleMapper, vehiclePresenter, factory, serviceOrderPresenter,
                sagaOrchestratorUseCase, statusHistoryGateway, new SimpleMeterRegistry());
    }

    @Test
    void shouldDelegateFindAllToUseCase() {
        controller = build();
        when(serviceOrderGateway.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        assertTrue(controller.findAll().isEmpty());
    }

    @Test
    void shouldDelegateFindActiveToUseCase() {
        controller = build();
        when(serviceOrderGateway.findActiveOrdersSortedByPriority()).thenReturn(List.of());

        assertTrue(controller.findActiveSortedByPriority().isEmpty());
    }

    @Test
    void shouldDelegateFindStatusByIdToUseCase() {
        controller = build();
        UUID id = UUID.randomUUID();
        ServiceOrder order = mock(ServiceOrder.class);
        when(order.getStatus()).thenReturn(com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum.RECEBIDO);
        when(serviceOrderGateway.findById(id)).thenReturn(Optional.of(order));

        assertEquals("Recebido", controller.findStatusById(id));
    }

    @Test
    void shouldDelegateFindHistoryByIdToUseCase() {
        controller = build();
        UUID id = UUID.randomUUID();
        when(serviceOrderGateway.findById(id)).thenReturn(Optional.of(mock(ServiceOrder.class)));
        when(statusHistoryGateway.findByServiceOrderId(id)).thenReturn(List.of());

        assertTrue(controller.findHistoryById(id).isEmpty());
    }
}
