package com.fiap.mechanical_hub.core_service.infrastructure.http.presenters;

import com.fiap.mechanical_hub.core_service.application.dto.customer.CustomerResponse;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderCustomerOutput;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderCustomerView;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderDetailOutput;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderDetailResponse;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderResponse;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderSummaryOutput;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderSummaryResponse;
import com.fiap.mechanical_hub.core_service.application.dto.vehicle.VehicleResponse;
import com.fiap.mechanical_hub.core_service.application.presenters.ServiceOrderPresenter;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;
import org.springframework.stereotype.Component;

import static com.fiap.mechanical_hub.core_service.shared.utils.document.DocumentFormatter.formatDocument;
import static com.fiap.mechanical_hub.core_service.shared.utils.telephone.TelephoneFormatter.formatTelephone;

@Component
public class ServiceOrderPresenterImpl implements ServiceOrderPresenter {

    @Override
    public ServiceOrderResponse toResponse(ServiceOrder order) {
        return new ServiceOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus().getDisplayName(),
                order.getVehicleId(),
                order.getCustomerId(),
                order.getCreatedByUserId(),
                order.getResponsibleUserId(),
                order.getRequestDescription(),
                order.getBudget(),
                order.getEstimatedCompletionAt(),
                order.getOpenedAt(),
                order.getCompletedAt(),
                order.getDeliveredAt(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    @Override
    public ServiceOrderDetailResponse toDetailResponse(ServiceOrderDetailOutput output) {
        return new ServiceOrderDetailResponse(
                output.order().getId(),
                output.order().getOrderNumber(),
                output.order().getStatus().getDisplayName(),
                toCustomerResponse(output.customer()),
                toVehicleResponse(output.vehicle()),
                output.order().getRequestDescription(),
                output.order().getBudget(),
                output.order().getCreatedAt()
        );
    }

    @Override
    public ServiceOrderSummaryResponse toSummaryResponse(ServiceOrderSummaryOutput output) {
        VehicleResponse vehicleResponse = toVehicleResponse(output.vehicle());
        String vehicleInfo = vehicleResponse.brand() + " " + vehicleResponse.model() + " (" + vehicleResponse.licensePlate() + ")";
        return new ServiceOrderSummaryResponse(
                output.order().getId(),
                output.order().getOrderNumber(),
                output.order().getStatus().getDisplayName(),
                toCustomerResponse(output.customer()).name(),
                vehicleInfo,
                output.order().getCreatedAt()
        );
    }

    @Override
    public ServiceOrderCustomerView toCustomerView(ServiceOrderCustomerOutput output) {
        VehicleResponse vehicleResponse = toVehicleResponse(output.vehicle());
        return new ServiceOrderCustomerView(
                output.order().getOrderNumber(),
                toCustomerResponse(output.customer()).name(),
                vehicleResponse.licensePlate(),
                vehicleResponse.model(),
                vehicleResponse.brand(),
                output.order().getStatus().getDisplayName(),
                output.order().getBudget(),
                output.order().getOpenedAt(),
                output.order().getCompletedAt(),
                output.order().getDeliveredAt()
        );
    }

    private CustomerResponse toCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getDocumentTypeEnum().getValue(),
                formatDocument(customer.getDocumentTypeEnum().getValue(), customer.getDocumentNumber()),
                formatTelephone(customer.getTelephone()),
                customer.getEmail(),
                customer.getAddress(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    private VehicleResponse toVehicleResponse(Vehicle vehicle) {
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
}
