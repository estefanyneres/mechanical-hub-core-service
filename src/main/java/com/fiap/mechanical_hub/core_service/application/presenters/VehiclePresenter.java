package com.fiap.mechanical_hub.core_service.application.presenters;

import com.fiap.mechanical_hub.core_service.application.dto.vehicle.VehicleResponse;
import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;

import java.util.List;

public interface VehiclePresenter {
    VehicleResponse toResponse(Vehicle vehicle);
    List<VehicleResponse> toResponseList(List<Vehicle> vehicles);
}
