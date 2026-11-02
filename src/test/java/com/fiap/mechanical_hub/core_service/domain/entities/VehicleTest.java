package com.fiap.mechanical_hub.core_service.domain.entities;

import com.fiap.mechanical_hub.core_service.domain.entities.constants.TestConstants;
import com.fiap.mechanical_hub.core_service.domain.entities.mocks.VehicleMock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    @Test
    void shouldCreateVehicleWithValidData() {
        Vehicle vehicle = VehicleMock.defaultVehicle();

        assertNotNull(vehicle.getId());
        assertEquals(TestConstants.DEFAULT_CUSTOMER_ID, vehicle.getCustomerId());
        assertEquals(TestConstants.DEFAULT_VEHICLE_LICENSE_PLATE, vehicle.getLicensePlate());
        assertEquals(TestConstants.DEFAULT_VEHICLE_BRAND, vehicle.getBrand());
        assertEquals(TestConstants.DEFAULT_VEHICLE_MODEL, vehicle.getModel());
        assertEquals(TestConstants.DEFAULT_VEHICLE_YEAR, vehicle.getYear());
        assertEquals(TestConstants.DEFAULT_VEHICLE_COLOR, vehicle.getColor());
        assertNotNull(vehicle.getCreatedAt());
        assertNotNull(vehicle.getUpdatedAt());
    }

    @Test
    void shouldNormalizeLicensePlateWhenCreating() {
        Vehicle vehicle = VehicleMock.vehicleWithDifferentLicensePlate("abc1234");

        assertNotNull(vehicle.getLicensePlate());
        assertFalse(vehicle.getLicensePlate().isEmpty());
    }

    @Test
    void shouldUpdateVehicle() {
        Vehicle vehicle = VehicleMock.defaultVehicle();
        String newBrand = "Honda";
        String newModel = "Civic";
        Integer newYear = 2021;

        vehicle.update(TestConstants.DEFAULT_VEHICLE_LICENSE_PLATE, newBrand, newModel, newYear, "Preto");

        assertEquals(newBrand, vehicle.getBrand());
        assertEquals(newModel, vehicle.getModel());
        assertEquals(newYear, vehicle.getYear());
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithInvalidLicensePlate() {
        assertThrows(Exception.class, () -> Vehicle.create(
                TestConstants.DEFAULT_CUSTOMER_ID, "invalid", TestConstants.DEFAULT_VEHICLE_BRAND,
                TestConstants.DEFAULT_VEHICLE_MODEL, TestConstants.DEFAULT_VEHICLE_YEAR, TestConstants.DEFAULT_VEHICLE_COLOR));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithInvalidLicensePlate() {
        Vehicle vehicle = VehicleMock.defaultVehicle();

        assertThrows(Exception.class, () -> vehicle.update(
                "invalid", TestConstants.DEFAULT_VEHICLE_BRAND, TestConstants.DEFAULT_VEHICLE_MODEL,
                TestConstants.DEFAULT_VEHICLE_YEAR, TestConstants.DEFAULT_VEHICLE_COLOR));
    }

    @Test
    void shouldCreateDifferentVehiclesWithDifferentLicensePlates() {
        Vehicle vehicle1 = VehicleMock.vehicleWithDifferentLicensePlate("ABC1234");
        Vehicle vehicle2 = VehicleMock.vehicleWithDifferentLicensePlate("XYZ9876");

        assertNotEquals(vehicle1.getId(), vehicle2.getId());
        assertNotEquals(vehicle1.getLicensePlate(), vehicle2.getLicensePlate());
    }
}
