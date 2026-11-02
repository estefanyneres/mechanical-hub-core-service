package com.fiap.mechanical_hub.core_service.application.usecases;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderNumberGeneratorTest {

    @Mock
    private ServiceOrderGateway serviceOrderGateway;

    @InjectMocks
    private OrderNumberGenerator orderNumberGenerator;

    private String currentYearMonth;

    @BeforeEach
    void setUp() {
        currentYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    @Test
    void generate_ShouldReturnFirstSequence_WhenNoPreviousOrders() {
        when(serviceOrderGateway.findLastOrderNumberByYearMonth(anyString())).thenReturn(Optional.empty());

        String result = orderNumberGenerator.generate();

        assertEquals(String.format("OS-%s-0001", currentYearMonth), result);
    }

    @Test
    void generate_ShouldIncrementSequence_WhenPreviousOrderExists() {
        String lastOrderNumber = String.format("OS-%s-0042", currentYearMonth);
        when(serviceOrderGateway.findLastOrderNumberByYearMonth(currentYearMonth)).thenReturn(Optional.of(lastOrderNumber));

        String result = orderNumberGenerator.generate();

        assertEquals(String.format("OS-%s-0043", currentYearMonth), result);
    }

    @Test
    void generate_ShouldHandleLargeSequences() {
        String lastOrderNumber = String.format("OS-%s-9999", currentYearMonth);
        when(serviceOrderGateway.findLastOrderNumberByYearMonth(currentYearMonth)).thenReturn(Optional.of(lastOrderNumber));

        String result = orderNumberGenerator.generate();

        assertEquals(String.format("OS-%s-10000", currentYearMonth), result);
    }
}
