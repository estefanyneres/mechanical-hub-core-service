package com.fiap.mechanical_hub.core_service.bdd.fakes;

import com.fiap.mechanical_hub.core_service.application.gateways.VehicleGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryVehicleGateway implements VehicleGateway {

    private final Map<UUID, Vehicle> store = new LinkedHashMap<>();

    @Override
    public Vehicle save(Vehicle vehicle) {
        store.put(vehicle.getId(), vehicle);
        return vehicle;
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Vehicle> findByLicensePlate(String licensePlate) {
        return store.values().stream()
                .filter(v -> v.getLicensePlate().equals(licensePlate))
                .findFirst();
    }

    @Override
    public List<Vehicle> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(UUID id) {
        store.remove(id);
    }

    @Override
    public boolean existsByLicensePlate(String licensePlate) {
        return findByLicensePlate(licensePlate).isPresent();
    }

    @Override
    public boolean existsByLicensePlateAndIdNot(String licensePlate, UUID id) {
        return store.values().stream()
                .anyMatch(v -> v.getLicensePlate().equals(licensePlate) && !v.getId().equals(id));
    }
}
