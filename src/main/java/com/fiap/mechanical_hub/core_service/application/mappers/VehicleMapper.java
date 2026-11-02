package com.fiap.mechanical_hub.core_service.application.mappers;

import com.fiap.mechanical_hub.core_service.application.dto.vehicle.UpsertVehicleRequest;
import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;

import java.util.UUID;

public class VehicleMapper {

    public Vehicle toDomainEntity(UUID customerId, UpsertVehicleRequest request) {
        return Vehicle.create(
                customerId,
                request.licensePlate(),
                request.brand(),
                request.model(),
                request.year(),
                request.color()
        );
    }

    public Vehicle toDomainEntity(UpsertVehicleRequest request, Vehicle existingVehicle) {
        existingVehicle.update(
                request.licensePlate(),
                request.brand(),
                request.model(),
                request.year(),
                request.color()
        );
        return existingVehicle;
    }
}
