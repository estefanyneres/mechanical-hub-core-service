package com.fiap.mechanical_hub.core_service.application.usecases;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderGateway;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class OrderNumberGenerator {

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final ServiceOrderGateway serviceOrderGateway;

    public OrderNumberGenerator(ServiceOrderGateway serviceOrderGateway) {
        this.serviceOrderGateway = serviceOrderGateway;
    }

    public synchronized String generate() {
        String yearMonth = LocalDate.now().format(YEAR_MONTH_FORMATTER);

        int nextSequence = serviceOrderGateway
                .findLastOrderNumberByYearMonth(yearMonth)
                .map(last -> extractSequence(last) + 1)
                .orElse(1);

        return String.format("OS-%s-%04d", yearMonth, nextSequence);
    }

    private int extractSequence(String orderNumber) {
        String[] parts = orderNumber.split("-");
        return Integer.parseInt(parts[2]);
    }
}
