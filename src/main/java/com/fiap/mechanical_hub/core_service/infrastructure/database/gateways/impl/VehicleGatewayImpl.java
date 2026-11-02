package com.fiap.mechanical_hub.core_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.core_service.application.gateways.VehicleGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.CustomerModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.VehicleModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.repositories.VehicleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VehicleGatewayImpl implements VehicleGateway {

    private final VehicleJpaRepository jpaRepository;

    @Override
    public Vehicle save(Vehicle vehicle) {
        VehicleModel entity = toJpaEntity(vehicle);
        VehicleModel saved = jpaRepository.save(entity);
        return toDomainEntity(saved);
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomainEntity);
    }

    @Override
    public Optional<Vehicle> findByLicensePlate(String licensePlate) {
        return jpaRepository.findByLicensePlate(licensePlate).map(this::toDomainEntity);
    }

    @Override
    public List<Vehicle> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomainEntity)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByLicensePlate(String licensePlate) {
        return jpaRepository.existsByLicensePlate(licensePlate);
    }

    @Override
    public boolean existsByLicensePlateAndIdNot(String licensePlate, UUID id) {
        return jpaRepository.existsByLicensePlateAndIdNot(licensePlate, id);
    }

    private VehicleModel toJpaEntity(Vehicle vehicle) {
        CustomerModel customerRef = new CustomerModel();
        customerRef.setId(vehicle.getCustomerId());

        return new VehicleModel(
                vehicle.getId(),
                customerRef,
                vehicle.getLicensePlate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getColor(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }

    private Vehicle toDomainEntity(VehicleModel entity) {
        return new Vehicle(
                entity.getId(),
                entity.getCustomer().getId(),
                entity.getLicensePlate(),
                entity.getBrand(),
                entity.getModel(),
                entity.getYear(),
                entity.getColor(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
