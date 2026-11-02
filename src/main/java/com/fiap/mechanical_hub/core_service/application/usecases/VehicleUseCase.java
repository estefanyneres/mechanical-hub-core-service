package com.fiap.mechanical_hub.core_service.application.usecases;

import com.fiap.mechanical_hub.core_service.application.dto.vehicle.UpsertVehicleRequest;
import com.fiap.mechanical_hub.core_service.application.dto.vehicle.VehicleResponse;
import com.fiap.mechanical_hub.core_service.application.gateways.CustomerGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.VehicleGateway;
import com.fiap.mechanical_hub.core_service.application.mappers.VehicleMapper;
import com.fiap.mechanical_hub.core_service.application.presenters.VehiclePresenter;
import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;
import com.fiap.mechanical_hub.core_service.domain.exceptions.DuplicateLicensePlateException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static com.fiap.mechanical_hub.core_service.shared.utils.license_plate.LicensePlateFormatter.normalize;
import static com.fiap.mechanical_hub.core_service.shared.utils.license_plate.LicensePlateValidator.validateLicensePlate;

public class VehicleUseCase {

    public static final String VEICULO_NAO_ENCONTRADO_PARA_O_ID = "Veículo não encontrado para o id: ";
    private final VehicleGateway vehicleGateway;
    private final CustomerGateway customerGateway;
    private final VehicleMapper vehicleMapper;
    private final VehiclePresenter vehiclePresenter;

    public VehicleUseCase(VehicleGateway vehicleGateway, CustomerGateway customerGateway,
                          VehicleMapper vehicleMapper, VehiclePresenter vehiclePresenter) {
        this.vehicleGateway = vehicleGateway;
        this.customerGateway = customerGateway;
        this.vehicleMapper = vehicleMapper;
        this.vehiclePresenter = vehiclePresenter;
    }

    public VehicleResponse create(UUID customerId, UpsertVehicleRequest request) {
        if (customerGateway.findById(customerId).isEmpty()) {
            throw new NoSuchElementException("Cliente não encontrado para o id: " + customerId);
        }

        validateLicensePlate(request.licensePlate());
        String normalizedPlate = normalize(request.licensePlate());

        if (vehicleGateway.existsByLicensePlate(normalizedPlate)) {
            throw new DuplicateLicensePlateException(
                    String.format("Veículo com placa %s já existe", normalizedPlate)
            );
        }

        Vehicle vehicle = vehicleMapper.toDomainEntity(customerId, request);
        Vehicle saved = vehicleGateway.save(vehicle);
        return vehiclePresenter.toResponse(saved);
    }

    public Vehicle findById(UUID id) {
        return vehicleGateway.findById(id)
                .orElseThrow(() -> new NoSuchElementException(VEICULO_NAO_ENCONTRADO_PARA_O_ID + id));
    }

    public VehicleResponse findByIdAndPresent(UUID id) {
        return vehiclePresenter.toResponse(findById(id));
    }

    public List<VehicleResponse> findAll() {
        return vehiclePresenter.toResponseList(vehicleGateway.findAll());
    }

    public VehicleResponse update(UUID id, UpsertVehicleRequest request) {
        Vehicle existingVehicle = vehicleGateway.findById(id)
                .orElseThrow(() -> new NoSuchElementException(VEICULO_NAO_ENCONTRADO_PARA_O_ID + id));

        validateLicensePlate(request.licensePlate());
        String normalizedPlate = normalize(request.licensePlate());

        if (vehicleGateway.existsByLicensePlateAndIdNot(normalizedPlate, id)) {
            throw new DuplicateLicensePlateException(
                    String.format("Veículo com placa %s já existe", normalizedPlate)
            );
        }

        Vehicle updatedVehicle = vehicleMapper.toDomainEntity(request, existingVehicle);
        Vehicle saved = vehicleGateway.save(updatedVehicle);
        return vehiclePresenter.toResponse(saved);
    }

    public void delete(UUID id) {
        if (vehicleGateway.findById(id).isEmpty()) {
            throw new NoSuchElementException(VEICULO_NAO_ENCONTRADO_PARA_O_ID + id);
        }
        vehicleGateway.deleteById(id);
    }

    public Vehicle findByLicensePlateOrCreate(UUID customerId, String licensePlate, String brand,
                                               String model, Integer year, String color) {
        validateLicensePlate(licensePlate);
        String normalizedPlate = normalize(licensePlate);

        Optional<Vehicle> existingVehicle = vehicleGateway.findByLicensePlate(normalizedPlate);

        if (existingVehicle.isPresent()) {
            return existingVehicle.get();
        }

        Vehicle newVehicle = Vehicle.create(customerId, normalizedPlate, brand, model, year, color);
        return vehicleGateway.save(newVehicle);
    }
}
