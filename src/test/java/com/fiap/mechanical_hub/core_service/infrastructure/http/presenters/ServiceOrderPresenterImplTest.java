package com.fiap.mechanical_hub.core_service.infrastructure.http.presenters;

import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.*;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;
import com.fiap.mechanical_hub.core_service.domain.entities.mocks.CustomerMock;
import com.fiap.mechanical_hub.core_service.domain.entities.mocks.ServiceOrderMock;
import com.fiap.mechanical_hub.core_service.domain.entities.mocks.VehicleMock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceOrderPresenterImplTest {

    private final ServiceOrderPresenterImpl presenter = new ServiceOrderPresenterImpl();

    @Test
    void shouldConvertToResponse() {
        ServiceOrder order = ServiceOrderMock.defaultServiceOrder();

        ServiceOrderResponse response = presenter.toResponse(order);

        assertEquals(order.getId(), response.id());
        assertEquals(order.getOrderNumber(), response.orderNumber());
        assertEquals("Recebido", response.status());
    }

    @Test
    void shouldConvertToDetailResponse() {
        ServiceOrder order = ServiceOrderMock.defaultServiceOrder();
        Vehicle vehicle = VehicleMock.defaultVehicle();
        Customer customer = CustomerMock.defaultCustomer();

        ServiceOrderDetailResponse response = presenter.toDetailResponse(
                new ServiceOrderDetailOutput(order, vehicle, customer));

        assertEquals(order.getOrderNumber(), response.orderNumber());
        assertEquals(customer.getName(), response.customer().name());
        assertEquals(vehicle.getLicensePlate(), response.vehicle().licensePlate());
    }

    @Test
    void shouldConvertToSummaryResponse() {
        ServiceOrder order = ServiceOrderMock.defaultServiceOrder();
        Vehicle vehicle = VehicleMock.defaultVehicle();
        Customer customer = CustomerMock.defaultCustomer();

        ServiceOrderSummaryResponse response = presenter.toSummaryResponse(
                new ServiceOrderSummaryOutput(order, customer, vehicle));

        assertEquals(order.getOrderNumber(), response.orderNumber());
        assertTrue(response.vehicleInfo().contains(vehicle.getBrand()));
    }

    @Test
    void shouldConvertToCustomerView() {
        ServiceOrder order = ServiceOrderMock.defaultServiceOrder();
        Vehicle vehicle = VehicleMock.defaultVehicle();
        Customer customer = CustomerMock.defaultCustomer();

        ServiceOrderCustomerView view = presenter.toCustomerView(
                new ServiceOrderCustomerOutput(order, vehicle, customer));

        assertEquals(order.getOrderNumber(), view.orderNumber());
        assertEquals(customer.getName(), view.customerName());
        assertEquals(vehicle.getLicensePlate(), view.vehicleLicensePlate());
    }
}
