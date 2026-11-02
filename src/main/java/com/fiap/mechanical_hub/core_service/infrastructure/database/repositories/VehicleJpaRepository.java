package com.fiap.mechanical_hub.core_service.infrastructure.database.repositories;

import com.fiap.mechanical_hub.core_service.infrastructure.database.models.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleJpaRepository extends JpaRepository<VehicleModel, UUID> {

    Optional<VehicleModel> findByLicensePlate(String licensePlate);

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByLicensePlateAndIdNot(String licensePlate, UUID id);
}
