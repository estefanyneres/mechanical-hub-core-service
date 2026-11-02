package com.fiap.mechanical_hub.core_service.application.controllers;

import com.fiap.mechanical_hub.core_service.application.dto.vehicle.UpsertVehicleRequest;
import com.fiap.mechanical_hub.core_service.application.dto.vehicle.VehicleResponse;
import com.fiap.mechanical_hub.core_service.application.gateways.CustomerGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.VehicleGateway;
import com.fiap.mechanical_hub.core_service.application.mappers.VehicleMapper;
import com.fiap.mechanical_hub.core_service.application.presenters.VehiclePresenter;
import com.fiap.mechanical_hub.core_service.application.usecases.VehicleUseCase;

import java.util.List;
import java.util.UUID;

public class VehicleController {

    private final VehicleUseCase vehicleUseCase;

    public VehicleController(VehicleGateway vehicleGateway, CustomerGateway customerGateway,
                             VehicleMapper vehicleMapper, VehiclePresenter vehiclePresenter) {
        this.vehicleUseCase = new VehicleUseCase(vehicleGateway, customerGateway, vehicleMapper, vehiclePresenter);
    }

    public VehicleResponse create(UUID customerId, UpsertVehicleRequest request) {
        return vehicleUseCase.create(customerId, request);
    }

    public List<VehicleResponse> findAll() {
        return vehicleUseCase.findAll();
    }

    public VehicleResponse findById(UUID id) {
        return vehicleUseCase.findByIdAndPresent(id);
    }

    public VehicleResponse update(UUID id, UpsertVehicleRequest request) {
        return vehicleUseCase.update(id, request);
    }

    public void delete(UUID id) {
        vehicleUseCase.delete(id);
    }
}
