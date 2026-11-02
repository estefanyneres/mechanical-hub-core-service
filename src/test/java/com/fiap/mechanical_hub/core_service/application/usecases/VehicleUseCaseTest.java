package com.fiap.mechanical_hub.core_service.application.usecases;

import com.fiap.mechanical_hub.core_service.application.dto.vehicle.UpsertVehicleRequest;
import com.fiap.mechanical_hub.core_service.application.dto.vehicle.VehicleResponse;
import com.fiap.mechanical_hub.core_service.application.gateways.CustomerGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.VehicleGateway;
import com.fiap.mechanical_hub.core_service.application.mappers.VehicleMapper;
import com.fiap.mechanical_hub.core_service.application.presenters.VehiclePresenter;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;
import com.fiap.mechanical_hub.core_service.domain.exceptions.DuplicateLicensePlateException;
import com.fiap.mechanical_hub.core_service.domain.exceptions.InvalidLicensePlateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleUseCaseTest {

    @Mock
    private VehicleGateway vehicleGateway;

    @Mock
    private CustomerGateway customerGateway;

    @Spy
    private VehicleMapper vehicleMapper;

    @Mock
    private VehiclePresenter vehiclePresenter;

    @InjectMocks
    private VehicleUseCase vehicleUseCase;

    @Test
    void shouldCreateVehicleSuccessfully() {
        UUID customerId = UUID.randomUUID();
        UpsertVehicleRequest request = new UpsertVehicleRequest("ABC1234", "Fiat", "Uno", 2010, "Escada");
        VehicleResponse expectedResponse = new VehicleResponse(UUID.randomUUID(), customerId, "ABC1234", "Fiat", "Uno", 2010, "Escada", null, null);

        when(customerGateway.findById(customerId)).thenReturn(Optional.of(new Customer()));
        when(vehicleGateway.existsByLicensePlate("ABC1234")).thenReturn(false);
        when(vehicleGateway.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));
        when(vehiclePresenter.toResponse(any(Vehicle.class))).thenReturn(expectedResponse);

        VehicleResponse response = vehicleUseCase.create(customerId, request);

        assertThat(response.licensePlate()).isEqualTo("ABC1234");
        verify(vehicleGateway).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        UUID customerId = UUID.randomUUID();
        UpsertVehicleRequest request = new UpsertVehicleRequest("ABC1234", "Fiat", "Uno", 2010, "Prata");
        when(customerGateway.findById(customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleUseCase.create(customerId, request))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void shouldThrowExceptionForInvalidPlate() {
        UUID customerId = UUID.randomUUID();
        UpsertVehicleRequest request = new UpsertVehicleRequest("PLACA-INVALIDA", "Ford", "Ka", 2020, "Preto");
        when(customerGateway.findById(customerId)).thenReturn(Optional.of(new Customer()));

        assertThatThrownBy(() -> vehicleUseCase.create(customerId, request))
                .isInstanceOf(InvalidLicensePlateException.class);
    }

    @Test
    void shouldThrowExceptionForDuplicatePlateOnCreate() {
        UUID customerId = UUID.randomUUID();
        UpsertVehicleRequest request = new UpsertVehicleRequest("ABC1234", "Fiat", "Uno", 2010, "Prata");
        when(customerGateway.findById(customerId)).thenReturn(Optional.of(new Customer()));
        when(vehicleGateway.existsByLicensePlate("ABC1234")).thenReturn(true);

        assertThatThrownBy(() -> vehicleUseCase.create(customerId, request))
                .isInstanceOf(DuplicateLicensePlateException.class);
    }

    @Test
    void shouldUpdateVehicleSuccessfully() {
        UUID vehicleId = UUID.randomUUID();
        var existingVehicle = new Vehicle(vehicleId, null, "OLD1234", "Honda", "Civic", 2018, "Preto", null, null);
        UpsertVehicleRequest updateRequest = new UpsertVehicleRequest("NEW1234", "Toyota", "Corolla", 2022, "Branco");
        VehicleResponse expectedResponse = new VehicleResponse(vehicleId, null, "NEW1234", "Toyota", "Corolla", 2022, "Branco", null, null);

        when(vehicleGateway.findById(vehicleId)).thenReturn(Optional.of(existingVehicle));
        when(vehicleGateway.existsByLicensePlateAndIdNot("NEW1234", vehicleId)).thenReturn(false);
        when(vehicleGateway.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));
        when(vehiclePresenter.toResponse(any(Vehicle.class))).thenReturn(expectedResponse);

        VehicleResponse response = vehicleUseCase.update(vehicleId, updateRequest);

        assertThat(response.licensePlate()).isEqualTo("NEW1234");
    }

    @Test
    void shouldFindByPlateOrCreateNew() {
        UUID customerId = UUID.randomUUID();
        when(vehicleGateway.findByLicensePlate("ABC1234")).thenReturn(Optional.empty());
        when(vehicleGateway.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        Vehicle result = vehicleUseCase.findByLicensePlateOrCreate(customerId, "ABC1234", "VW", "Gol", 2015, "Azul");

        assertThat(result.getLicensePlate()).isEqualTo("ABC1234");
        verify(vehicleGateway).save(any(Vehicle.class));
    }

    @Test
    void shouldDeleteVehicle() {
        UUID vehicleId = UUID.randomUUID();
        when(vehicleGateway.findById(vehicleId)).thenReturn(Optional.of(new Vehicle()));

        vehicleUseCase.delete(vehicleId);

        verify(vehicleGateway).deleteById(vehicleId);
    }

    @Test
    void findById_Success() {
        UUID vehicleId = UUID.randomUUID();
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicleGateway.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        Vehicle result = vehicleUseCase.findById(vehicleId);

        assertEquals(vehicle, result);
    }

    @Test
    void findById_NotFound() {
        UUID vehicleId = UUID.randomUUID();
        when(vehicleGateway.findById(vehicleId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> vehicleUseCase.findById(vehicleId));
    }

    @Test
    void findAll_Success() {
        Vehicle v1 = mock(Vehicle.class);
        Vehicle v2 = mock(Vehicle.class);
        List<Vehicle> vehicles = List.of(v1, v2);
        VehicleResponse r1 = new VehicleResponse(UUID.randomUUID(), null, "ABC1234", null, null, null, null, null, null);
        VehicleResponse r2 = new VehicleResponse(UUID.randomUUID(), null, "XYZ5678", null, null, null, null, null, null);
        when(vehicleGateway.findAll()).thenReturn(vehicles);
        when(vehiclePresenter.toResponseList(vehicles)).thenReturn(List.of(r1, r2));

        List<VehicleResponse> result = vehicleUseCase.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void update_ShouldThrowException_WhenLicensePlateAlreadyExistsForAnotherVehicle() {
        UUID vehicleId = UUID.randomUUID();
        String plate = "ABC1D23";
        UpsertVehicleRequest request = new UpsertVehicleRequest(plate, null, null, null, null);
        Vehicle existingVehicle = mock(Vehicle.class);

        when(vehicleGateway.findById(vehicleId)).thenReturn(Optional.of(existingVehicle));
        when(vehicleGateway.existsByLicensePlateAndIdNot(plate, vehicleId)).thenReturn(true);

        assertThrows(DuplicateLicensePlateException.class, () -> vehicleUseCase.update(vehicleId, request));
        verify(vehicleGateway, never()).save(any(Vehicle.class));
    }

    @Test
    void delete_ShouldThrowException_WhenVehicleDoesNotExist() {
        UUID vehicleId = UUID.randomUUID();
        when(vehicleGateway.findById(vehicleId)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> vehicleUseCase.delete(vehicleId));
        assertEquals(VehicleUseCase.VEICULO_NAO_ENCONTRADO_PARA_O_ID + vehicleId, exception.getMessage());
    }

    @Test
    void findByLicensePlateOrCreate_ShouldReturnExistingVehicle() {
        UUID customerId = UUID.randomUUID();
        String plate = "BRA2E24";
        Vehicle existingVehicle = Vehicle.create(customerId, plate, "Toyota", "Corolla", 2023, "Preto");
        when(vehicleGateway.findByLicensePlate(plate)).thenReturn(Optional.of(existingVehicle));

        Vehicle result = vehicleUseCase.findByLicensePlateOrCreate(customerId, plate, "Toyota", "Corolla", 2023, "Preto");

        assertEquals(existingVehicle, result);
        verify(vehicleGateway, never()).save(any(Vehicle.class));
    }
}
