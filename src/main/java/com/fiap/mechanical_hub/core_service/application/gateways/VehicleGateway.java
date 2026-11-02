package com.fiap.mechanical_hub.core_service.application.gateways;

import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleGateway {

    Vehicle save(Vehicle vehicle);

    Optional<Vehicle> findById(UUID id);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    List<Vehicle> findAll();

    void deleteById(UUID id);

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByLicensePlateAndIdNot(String licensePlate, UUID id);
}
