package com.fiap.mechanical_hub.core_service.infrastructure.http.presenters;

import com.fiap.mechanical_hub.core_service.application.dto.vehicle.VehicleResponse;
import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;
import com.fiap.mechanical_hub.core_service.domain.entities.mocks.VehicleMock;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VehiclePresenterImplTest {

    private final VehiclePresenterImpl presenter = new VehiclePresenterImpl();

    @Test
    void shouldConvertVehicleToResponse() {
        Vehicle vehicle = VehicleMock.defaultVehicle();

        VehicleResponse response = presenter.toResponse(vehicle);

        assertEquals(vehicle.getId(), response.id());
        assertEquals(vehicle.getLicensePlate(), response.licensePlate());
        assertEquals(vehicle.getBrand(), response.brand());
    }

    @Test
    void shouldConvertListOfVehiclesToResponseList() {
        List<VehicleResponse> result = presenter.toResponseList(List.of(VehicleMock.defaultVehicle()));

        assertEquals(1, result.size());
    }
}
