package com.fiap.mechanical_hub.core_service.application.controllers;

import com.fiap.mechanical_hub.core_service.application.dto.vehicle.UpsertVehicleRequest;
import com.fiap.mechanical_hub.core_service.application.dto.vehicle.VehicleResponse;
import com.fiap.mechanical_hub.core_service.application.gateways.CustomerGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.VehicleGateway;
import com.fiap.mechanical_hub.core_service.application.mappers.VehicleMapper;
import com.fiap.mechanical_hub.core_service.application.presenters.VehiclePresenter;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;
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
class VehicleControllerTest {

    @Mock private VehicleGateway vehicleGateway;
    @Mock private CustomerGateway customerGateway;
    @Mock private VehicleMapper vehicleMapper;
    @Mock private VehiclePresenter vehiclePresenter;

    private VehicleController controller;

    @Test
    void shouldDelegateCreateToUseCase() {
        controller = new VehicleController(vehicleGateway, customerGateway, vehicleMapper, vehiclePresenter);
        UUID customerId = UUID.randomUUID();
        UpsertVehicleRequest request = new UpsertVehicleRequest("ABC1234", "Fiat", "Uno", 2010, "Prata");
        Vehicle vehicle = Vehicle.create(customerId, "ABC1234", "Fiat", "Uno", 2010, "Prata");
        VehicleResponse response = mock(VehicleResponse.class);

        when(customerGateway.findById(customerId)).thenReturn(Optional.of(mock(Customer.class)));
        when(vehicleGateway.existsByLicensePlate("ABC1234")).thenReturn(false);
        when(vehicleMapper.toDomainEntity(customerId, request)).thenReturn(vehicle);
        when(vehicleGateway.save(vehicle)).thenReturn(vehicle);
        when(vehiclePresenter.toResponse(vehicle)).thenReturn(response);

        assertEquals(response, controller.create(customerId, request));
    }

    @Test
    void shouldDelegateFindAllToUseCase() {
        controller = new VehicleController(vehicleGateway, customerGateway, vehicleMapper, vehiclePresenter);
        when(vehicleGateway.findAll()).thenReturn(List.of());
        when(vehiclePresenter.toResponseList(List.of())).thenReturn(List.of());

        assertTrue(controller.findAll().isEmpty());
    }

    @Test
    void shouldDelegateFindByIdToUseCase() {
        controller = new VehicleController(vehicleGateway, customerGateway, vehicleMapper, vehiclePresenter);
        UUID id = UUID.randomUUID();
        Vehicle vehicle = mock(Vehicle.class);
        VehicleResponse response = mock(VehicleResponse.class);
        when(vehicleGateway.findById(id)).thenReturn(Optional.of(vehicle));
        when(vehiclePresenter.toResponse(vehicle)).thenReturn(response);

        assertEquals(response, controller.findById(id));
    }

    @Test
    void shouldDelegateDeleteToUseCase() {
        controller = new VehicleController(vehicleGateway, customerGateway, vehicleMapper, vehiclePresenter);
        UUID id = UUID.randomUUID();
        when(vehicleGateway.findById(id)).thenReturn(Optional.of(mock(Vehicle.class)));

        controller.delete(id);

        verify(vehicleGateway).deleteById(id);
    }
}
