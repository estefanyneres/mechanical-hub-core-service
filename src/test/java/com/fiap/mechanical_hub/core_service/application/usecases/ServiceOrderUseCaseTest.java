package com.fiap.mechanical_hub.core_service.application.usecases;

import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.CreateServiceOrderRequest;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.CustomerData;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderResponse;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderStatusHistoryResponse;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.VehicleData;
import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.application.presenters.ServiceOrderPresenter;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrderStatusHistory;
import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;
import com.fiap.mechanical_hub.core_service.domain.enums.DocumentTypeEnum;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import com.fiap.mechanical_hub.core_service.domain.exceptions.NotFoundException;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.OrderStatusTransition;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.OrderStatusTransitionFactory;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOrderUseCaseTest {

    @Mock private ServiceOrderGateway serviceOrderGateway;
    @Mock private CustomerUseCase customerUseCase;
    @Mock private VehicleUseCase vehicleUseCase;
    @Mock private OrderNumberGenerator orderNumberGenerator;
    @Mock private OrderStatusTransitionFactory factory;
    @Mock private ServiceOrderPresenter serviceOrderPresenter;
    @Mock private SagaOrchestratorUseCase sagaOrchestratorUseCase;
    @Mock private ServiceOrderStatusHistoryGateway statusHistoryGateway;

    private ServiceOrderUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID vehicleId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ServiceOrderUseCase(serviceOrderGateway, customerUseCase, vehicleUseCase,
                orderNumberGenerator, factory, serviceOrderPresenter, sagaOrchestratorUseCase,
                statusHistoryGateway, new SimpleMeterRegistry());
    }

    private CreateServiceOrderRequest createRequest() {
        return new CreateServiceOrderRequest(
                new CustomerData("José Silva", "CPF", "52998224725", "5511987654321", "jose@example.com", "Rua A"),
                new VehicleData("ABC1D23", "Toyota", "Corolla", 2020, "Branco"),
                "Barulho no motor");
    }

    @Test
    void shouldCreateServiceOrderWithoutTriggeringSaga() {
        Customer customer = Customer.create("José Silva", DocumentTypeEnum.CPF, "52998224725",
                "5511987654321", "jose@example.com", "Rua A");
        Vehicle vehicle = Vehicle.create(customer.getId(), "ABC1D23", "Toyota", "Corolla", 2020, "Branco");

        when(customerUseCase.findByDocumentOrCreate(any(), any(), any(), any(), any(), any())).thenReturn(customer);
        when(vehicleUseCase.findByLicensePlateOrCreate(any(), any(), any(), any(), any(), any())).thenReturn(vehicle);
        when(orderNumberGenerator.generate()).thenReturn("OS-202601-0001");
        when(serviceOrderGateway.save(any(ServiceOrder.class))).thenAnswer(i -> i.getArgument(0));
        when(serviceOrderPresenter.toResponse(any(ServiceOrder.class))).thenReturn(
                new ServiceOrderResponse(orderId, "OS-202601-0001", "Recebido", vehicle.getId(), customer.getId(),
                        userId, null, "Barulho no motor", null, null, null, null, null, LocalDateTime.now(), LocalDateTime.now()));

        ServiceOrderResponse result = useCase.create(createRequest(), userId);

        assertNotNull(result);
        assertEquals("OS-202601-0001", result.orderNumber());
        verifyNoInteractions(sagaOrchestratorUseCase);
    }

    @Test
    void shouldTriggerSagaWhenUpdatingStatusToAguardandoAprovacao() {
        ServiceOrder order = ServiceOrder.create(vehicleId, customerId, "OS-202601-0001", "desc", userId);
        order.startDiagnosis(userId);
        order.updateBudget(java.math.BigDecimal.TEN);

        OrderStatusTransition transition = mock(OrderStatusTransition.class);
        when(serviceOrderGateway.findById(order.getId())).thenReturn(Optional.of(order));
        when(factory.get(OrderStatusEnum.AGUARDANDO_APROVACAO)).thenReturn(transition);
        when(serviceOrderGateway.save(order)).thenReturn(order);
        doAnswer(invocation -> {
            order.submitForApproval();
            return null;
        }).when(transition).execute(order, userId);

        useCase.updateOrderStatus(order.getId(), "AGUARDANDO_APROVACAO", userId);

        verify(sagaOrchestratorUseCase).requestBudget(order);
    }

    @Test
    void shouldNotTriggerSagaWhenUpdatingToOtherStatus() {
        ServiceOrder order = ServiceOrder.create(vehicleId, customerId, "OS-202601-0001", "desc", userId);
        OrderStatusTransition transition = mock(OrderStatusTransition.class);

        when(serviceOrderGateway.findById(order.getId())).thenReturn(Optional.of(order));
        when(factory.get(OrderStatusEnum.EM_DIAGNOSTICO)).thenReturn(transition);
        when(serviceOrderGateway.save(order)).thenReturn(order);

        useCase.updateOrderStatus(order.getId(), "EM_DIAGNOSTICO", userId);

        verifyNoInteractions(sagaOrchestratorUseCase);
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingStatusOfUnknownOrder() {
        when(serviceOrderGateway.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> useCase.updateOrderStatus(orderId, "EM_DIAGNOSTICO", userId));
    }

    @Test
    void shouldFindHistoryById() {
        ServiceOrder order = ServiceOrder.create(vehicleId, customerId, "OS-202601-0001", "desc", userId);
        ServiceOrderStatusHistory history = ServiceOrderStatusHistory.register(
                order.getId(), OrderStatusEnum.RECEBIDO, OrderStatusEnum.EM_DIAGNOSTICO, userId);

        when(serviceOrderGateway.findById(order.getId())).thenReturn(Optional.of(order));
        when(statusHistoryGateway.findByServiceOrderId(order.getId())).thenReturn(List.of(history));

        List<ServiceOrderStatusHistoryResponse> result = useCase.findHistoryById(order.getId());

        assertEquals(1, result.size());
        assertEquals("Em diagnóstico", result.get(0).toStatus());
    }

    @Test
    void shouldThrowNotFoundWhenFindingHistoryOfUnknownOrder() {
        when(serviceOrderGateway.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> useCase.findHistoryById(orderId));
        verifyNoInteractions(statusHistoryGateway);
    }

    @Test
    void shouldFindStatusById() {
        ServiceOrder order = ServiceOrder.create(vehicleId, customerId, "OS-202601-0001", "desc", userId);
        when(serviceOrderGateway.findById(order.getId())).thenReturn(Optional.of(order));

        String status = useCase.findStatusById(order.getId());

        assertEquals("Recebido", status);
    }

    @Test
    void shouldThrowNotFoundWhenFindingStatusOfUnknownOrder() {
        when(serviceOrderGateway.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> useCase.findStatusById(orderId));
    }
}
