package com.fiap.mechanical_hub.core_service.infrastructure.http.presenters;

import com.fiap.mechanical_hub.core_service.application.dto.vehicle.VehicleResponse;
import com.fiap.mechanical_hub.core_service.application.presenters.VehiclePresenter;
import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VehiclePresenterImpl implements VehiclePresenter {

    @Override
    public VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getCustomerId(),
                vehicle.getLicensePlate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getColor(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }

    @Override
    public List<VehicleResponse> toResponseList(List<Vehicle> vehicles) {
        return vehicles.stream().map(this::toResponse).toList();
    }
}
