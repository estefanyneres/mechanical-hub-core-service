package com.fiap.mechanical_hub.core_service.domain.entities.mocks;

import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;

import static com.fiap.mechanical_hub.core_service.domain.entities.constants.TestConstants.*;

public class VehicleMock {

    public static Vehicle defaultVehicle() {
        return Vehicle.create(
                DEFAULT_CUSTOMER_ID,
                DEFAULT_VEHICLE_LICENSE_PLATE,
                DEFAULT_VEHICLE_BRAND,
                DEFAULT_VEHICLE_MODEL,
                DEFAULT_VEHICLE_YEAR,
                DEFAULT_VEHICLE_COLOR
        );
    }

    public static Vehicle vehicleWithDifferentLicensePlate(String licensePlate) {
        return Vehicle.create(
                DEFAULT_CUSTOMER_ID,
                licensePlate,
                DEFAULT_VEHICLE_BRAND,
                DEFAULT_VEHICLE_MODEL,
                DEFAULT_VEHICLE_YEAR,
                DEFAULT_VEHICLE_COLOR
        );
    }
}
