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
import com.fiap.mechanical_hub.core_service.application.usecases.SagaOrchestratorUseCase;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;
import com.fiap.mechanical_hub.core_service.domain.exceptions.NotFoundException;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.OrderStatusTransitionFactory;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalControllerTest {

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

    private ExternalController build() {
        return new ExternalController(serviceOrderGateway, customerGateway, customerMapper, customerPresenter,
                vehicleGateway, vehicleMapper, vehiclePresenter, factory, serviceOrderPresenter,
                sagaOrchestratorUseCase, statusHistoryGateway, new SimpleMeterRegistry());
    }

    @Test
    void shouldFindByOrderNumberWhenBelongsToCustomer() {
        ExternalController controller = build();
        UUID customerId = UUID.randomUUID();
        ServiceOrder order = ServiceOrder.create(UUID.randomUUID(), customerId, "OS-202601-0001", "desc", UUID.randomUUID());
        ServiceOrderCustomerView view = mock(ServiceOrderCustomerView.class);

        when(serviceOrderGateway.findByOrderNumber("OS-202601-0001")).thenReturn(Optional.of(order));
        when(vehicleGateway.findById(order.getVehicleId())).thenReturn(Optional.of(mock(Vehicle.class)));
        when(customerGateway.findById(customerId)).thenReturn(Optional.of(mock(Customer.class)));
        when(serviceOrderPresenter.toCustomerView(org.mockito.ArgumentMatchers.any())).thenReturn(view);

        assertEquals(view, controller.findByOrderNumber("OS-202601-0001", customerId));
    }

    @Test
    void shouldThrowNotFoundWhenOrderDoesNotBelongToCustomer() {
        ExternalController controller = build();
        UUID customerId = UUID.randomUUID();
        ServiceOrder order = ServiceOrder.create(UUID.randomUUID(), UUID.randomUUID(), "OS-202601-0001", "desc", UUID.randomUUID());

        when(serviceOrderGateway.findByOrderNumber("OS-202601-0001")).thenReturn(Optional.of(order));

        assertThrows(NotFoundException.class, () -> controller.findByOrderNumber("OS-202601-0001", customerId));
    }
}
