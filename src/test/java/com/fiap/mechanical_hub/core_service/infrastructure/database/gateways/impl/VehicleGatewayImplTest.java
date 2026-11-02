package com.fiap.mechanical_hub.core_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;
import com.fiap.mechanical_hub.core_service.domain.entities.mocks.VehicleMock;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.CustomerModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.VehicleModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.repositories.VehicleJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleGatewayImplTest {

    @Mock
    private VehicleJpaRepository jpaRepository;

    @InjectMocks
    private VehicleGatewayImpl gateway;

    private VehicleModel toModel(Vehicle vehicle) {
        CustomerModel customerModel = new CustomerModel();
        customerModel.setId(vehicle.getCustomerId());
        return new VehicleModel(vehicle.getId(), customerModel, vehicle.getLicensePlate(), vehicle.getBrand(),
                vehicle.getModel(), vehicle.getYear(), vehicle.getColor(), vehicle.getCreatedAt(), vehicle.getUpdatedAt());
    }

    @Test
    void shouldSaveVehicle() {
        Vehicle vehicle = VehicleMock.defaultVehicle();
        when(jpaRepository.save(any(VehicleModel.class))).thenAnswer(i -> i.getArgument(0));

        Vehicle result = gateway.save(vehicle);

        assertEquals(vehicle.getLicensePlate(), result.getLicensePlate());
    }

    @Test
    void shouldFindById() {
        Vehicle vehicle = VehicleMock.defaultVehicle();
        when(jpaRepository.findById(vehicle.getId())).thenReturn(Optional.of(toModel(vehicle)));

        assertTrue(gateway.findById(vehicle.getId()).isPresent());
    }

    @Test
    void shouldFindByLicensePlate() {
        Vehicle vehicle = VehicleMock.defaultVehicle();
        when(jpaRepository.findByLicensePlate(vehicle.getLicensePlate())).thenReturn(Optional.of(toModel(vehicle)));

        assertTrue(gateway.findByLicensePlate(vehicle.getLicensePlate()).isPresent());
    }

    @Test
    void shouldFindAll() {
        Vehicle vehicle = VehicleMock.defaultVehicle();
        when(jpaRepository.findAll()).thenReturn(List.of(toModel(vehicle)));

        assertEquals(1, gateway.findAll().size());
    }

    @Test
    void shouldDeleteById() {
        UUID id = UUID.randomUUID();

        gateway.deleteById(id);

        verify(jpaRepository).deleteById(id);
    }

    @Test
    void shouldCheckExistsByLicensePlate() {
        when(jpaRepository.existsByLicensePlate("ABC1D23")).thenReturn(true);

        assertTrue(gateway.existsByLicensePlate("ABC1D23"));
    }

    @Test
    void shouldCheckExistsByLicensePlateAndIdNot() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.existsByLicensePlateAndIdNot("ABC1D23", id)).thenReturn(false);

        assertFalse(gateway.existsByLicensePlateAndIdNot("ABC1D23", id));
    }
}
