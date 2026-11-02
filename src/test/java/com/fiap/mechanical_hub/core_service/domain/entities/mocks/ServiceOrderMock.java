package com.fiap.mechanical_hub.core_service.domain.entities.mocks;

import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;

import static com.fiap.mechanical_hub.core_service.domain.entities.constants.TestConstants.*;

public class ServiceOrderMock {

    public static ServiceOrder defaultServiceOrder() {
        return ServiceOrder.create(
                DEFAULT_VEHICLE_ID, DEFAULT_CUSTOMER_ID, DEFAULT_ORDER_NUMBER,
                DEFAULT_REQUEST_DESCRIPTION, DEFAULT_USER_ID);
    }

    public static ServiceOrder serviceOrderInDiagnosis() {
        ServiceOrder order = defaultServiceOrder();
        order.startDiagnosis(DEFAULT_USER_ID);
        return order;
    }

    public static ServiceOrder serviceOrderAwaitingApproval() {
        ServiceOrder order = serviceOrderInDiagnosis();
        order.updateBudget(DEFAULT_ORDER_BUDGET);
        order.submitForApproval();
        return order;
    }

    public static ServiceOrder serviceOrderApproved() {
        ServiceOrder order = serviceOrderAwaitingApproval();
        order.approve();
        return order;
    }

    public static ServiceOrder serviceOrderWithFailedPayment() {
        ServiceOrder order = serviceOrderApproved();
        order.failPayment();
        return order;
    }

    public static ServiceOrder servicePaymentRealized() {
        ServiceOrder order = serviceOrderApproved();
        order.confirmPayment();
        return order;
    }

    public static ServiceOrder serviceOrderReadyForExecution() {
        ServiceOrder order = servicePaymentRealized();
        order.markReadyForExecution();
        return order;
    }

    public static ServiceOrder serviceOrderStockPending() {
        ServiceOrder order = servicePaymentRealized();
        order.markStockPending();
        return order;
    }

    public static ServiceOrder serviceOrderInExecution() {
        ServiceOrder order = serviceOrderReadyForExecution();
        order.startExecution();
        return order;
    }

    public static ServiceOrder serviceOrderFinalized() {
        ServiceOrder order = serviceOrderInExecution();
        order.finish();
        return order;
    }
}
