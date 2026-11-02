package com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.mocks;

import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;

import java.math.BigDecimal;
import java.util.UUID;

public class ServiceOrderMock {

    private static final UUID VEHICLE_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String ORDER_NUMBER = "OS-202601-0001";
    private static final String DESCRIPTION = "Diagnóstico e reparo";

    private static ServiceOrder base() {
        return ServiceOrder.create(VEHICLE_ID, CUSTOMER_ID, ORDER_NUMBER, DESCRIPTION, USER_ID);
    }

    public static ServiceOrder receivedOrder() {
        return base();
    }

    public static ServiceOrder inDiagnosisOrderWithoutBudget() {
        ServiceOrder order = base();
        order.startDiagnosis(USER_ID);
        return order;
    }

    public static ServiceOrder inDiagnosisOrderWithBudget(BigDecimal budget) {
        ServiceOrder order = inDiagnosisOrderWithoutBudget();
        order.updateBudget(budget);
        return order;
    }

    public static ServiceOrder waitingApprovalOrder() {
        ServiceOrder order = inDiagnosisOrderWithBudget(BigDecimal.valueOf(500));
        order.submitForApproval();
        return order;
    }

    public static ServiceOrder approvedOrder() {
        ServiceOrder order = waitingApprovalOrder();
        order.approve();
        return order;
    }

    public static ServiceOrder failedPaymentOrder() {
        ServiceOrder order = approvedOrder();
        order.failPayment();
        return order;
    }

    public static ServiceOrder paymentRealizedOrder() {
        ServiceOrder order = approvedOrder();
        order.confirmPayment();
        return order;
    }

    public static ServiceOrder stockPendingOrder() {
        ServiceOrder order = paymentRealizedOrder();
        order.markStockPending();
        return order;
    }

    public static ServiceOrder readyForExecutionOrder() {
        ServiceOrder order = paymentRealizedOrder();
        order.markReadyForExecution();
        return order;
    }

    public static ServiceOrder inExecutionOrder() {
        ServiceOrder order = readyForExecutionOrder();
        order.startExecution();
        return order;
    }

    public static ServiceOrder finishedOrder() {
        ServiceOrder order = inExecutionOrder();
        order.finish();
        return order;
    }
}
