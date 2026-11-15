package com.fiap.mechanical_hub.core_service.application.usecases;

import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.CreateServiceOrderRequest;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderCustomerOutput;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderCustomerView;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderDetailOutput;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderDetailResponse;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderResponse;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderSummaryOutput;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderStatusHistoryResponse;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderSummaryResponse;
import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrderStatusHistory;
import com.fiap.mechanical_hub.core_service.application.presenters.ServiceOrderPresenter;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.entities.Vehicle;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import com.fiap.mechanical_hub.core_service.domain.exceptions.NotFoundException;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.OrderStatusTransitionFactory;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/**
 * Reduzida pra Fase 4 (TASK-015): sem processServices/addServices/
 * updateTaskStatus (repair-service agora) e sem approve/reject por
 * cliente (billing-service agora publica orcamento.aprovado/recusado,
 * que o SagaOrchestratorUseCase consome). create() NAO dispara o Saga --
 * nao da pra pedir orcamento antes do diagnostico existir. O Saga so
 * comeca quando o mecanico chama updateOrderStatus pra AGUARDANDO_APROVACAO
 * (mesma acao manual que ja existia no monolito), que entao publica
 * "orcamento.solicitado".
 */
@Slf4j
public class ServiceOrderUseCase {

    private final ServiceOrderGateway repository;
    private final CustomerUseCase customerUseCase;
    private final VehicleUseCase vehicleUseCase;
    private final OrderNumberGenerator orderNumberGenerator;
    private final OrderStatusTransitionFactory factory;
    private final ServiceOrderPresenter serviceOrderPresenter;
    private final SagaOrchestratorUseCase sagaOrchestratorUseCase;
    private final ServiceOrderStatusHistoryGateway statusHistoryGateway;
    private final MeterRegistry registry;

    public ServiceOrderUseCase(ServiceOrderGateway repository, CustomerUseCase customerUseCase,
                               VehicleUseCase vehicleUseCase, OrderNumberGenerator orderNumberGenerator,
                               OrderStatusTransitionFactory factory, ServiceOrderPresenter serviceOrderPresenter,
                               SagaOrchestratorUseCase sagaOrchestratorUseCase,
                               ServiceOrderStatusHistoryGateway statusHistoryGateway, MeterRegistry registry) {
        this.repository = repository;
        this.customerUseCase = customerUseCase;
        this.vehicleUseCase = vehicleUseCase;
        this.orderNumberGenerator = orderNumberGenerator;
        this.factory = factory;
        this.serviceOrderPresenter = serviceOrderPresenter;
        this.sagaOrchestratorUseCase = sagaOrchestratorUseCase;
        this.statusHistoryGateway = statusHistoryGateway;
        this.registry = registry;
    }

    public ServiceOrderResponse create(CreateServiceOrderRequest request, UUID createdByUserId) {
        var customerData = request.customer();
        Customer customer = customerUseCase.findByDocumentOrCreate(
                customerData.name(),
                customerData.documentType(),
                customerData.documentNumber(),
                customerData.telephone(),
                customerData.email(),
                customerData.address()
        );

        var vehicleData = request.vehicle();
        Vehicle vehicle = vehicleUseCase.findByLicensePlateOrCreate(
                customer.getId(),
                vehicleData.licensePlate(),
                vehicleData.brand(),
                vehicleData.model(),
                vehicleData.year(),
                vehicleData.color()
        );

        String orderNumber = orderNumberGenerator.generate();
        ServiceOrder serviceOrder = ServiceOrder.create(
                vehicle.getId(), customer.getId(), orderNumber, request.requestDescription(), createdByUserId);
        ServiceOrder saved = repository.save(serviceOrder);
        DistributionSummary.builder("service_order.creation.count").register(registry).record(1);
        log.info("Ordem de serviço {} criada para o cliente {}", saved.getOrderNumber(), customer.getId());

        return serviceOrderPresenter.toResponse(saved);
    }

    public ServiceOrder updateOrderStatus(UUID orderId, String orderStatus, UUID userId) {
        OrderStatusEnum targetStatus = OrderStatusEnum.fromString(orderStatus);
        ServiceOrder order = repository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada com ID: " + orderId));
        factory.get(targetStatus).execute(order, userId);
        ServiceOrder saved = repository.save(order);

        // Gatilho do Saga (TASK-016): so a partir daqui existe pedido de
        // orcamento, porque so agora o mecanico decidiu que o diagnostico
        // acabou. Garante que a OS ja esta em AGUARDANDO_APROVACAO antes de
        // qualquer reply do billing-service poder chegar.
        if (targetStatus == OrderStatusEnum.AGUARDANDO_APROVACAO) {
            sagaOrchestratorUseCase.requestBudget(saved);
        }

        return saved;
    }

    public List<ServiceOrderSummaryResponse> findAll() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::buildSummaryOutput)
                .map(serviceOrderPresenter::toSummaryResponse)
                .toList();
    }

    public List<ServiceOrderSummaryResponse> findActiveSortedByPriority() {
        return repository.findActiveOrdersSortedByPriority()
                .stream()
                .map(this::buildSummaryOutput)
                .map(serviceOrderPresenter::toSummaryResponse)
                .toList();
    }

    private ServiceOrderSummaryOutput buildSummaryOutput(ServiceOrder order) {
        Customer customer = customerUseCase.findById(order.getCustomerId());
        Vehicle vehicle = vehicleUseCase.findById(order.getVehicleId());
        return new ServiceOrderSummaryOutput(order, customer, vehicle);
    }

    public ServiceOrderDetailResponse findById(UUID id) {
        ServiceOrder order = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada com ID: " + id));

        Vehicle vehicle = vehicleUseCase.findById(order.getVehicleId());
        Customer customer = customerUseCase.findById(order.getCustomerId());

        ServiceOrderDetailOutput output = new ServiceOrderDetailOutput(order, vehicle, customer);
        return serviceOrderPresenter.toDetailResponse(output);
    }

    public String findStatusById(UUID id) {
        ServiceOrder order = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada com ID: " + id));
        return order.getStatus().getDisplayName();
    }

    public List<ServiceOrderStatusHistoryResponse> findHistoryById(UUID id) {
        if (!repository.findById(id).isPresent()) {
            throw new NotFoundException("Ordem de serviço não encontrada com ID: " + id);
        }
        return statusHistoryGateway.findByServiceOrderId(id).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private ServiceOrderStatusHistoryResponse toHistoryResponse(ServiceOrderStatusHistory history) {
        return new ServiceOrderStatusHistoryResponse(
                history.getFromStatus() != null ? history.getFromStatus().getDisplayName() : null,
                history.getToStatus().getDisplayName(),
                history.getChangedByUserId(),
                history.getChangedAt()
        );
    }

    public ServiceOrderCustomerView findByOrderNumber(String orderNumber, UUID customerId) {
        ServiceOrder order = repository.findByOrderNumber(orderNumber)
                .filter(o -> o.getCustomerId().equals(customerId))
                .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada com número: " + orderNumber));

        Vehicle vehicle = vehicleUseCase.findById(order.getVehicleId());
        Customer customer = customerUseCase.findById(order.getCustomerId());

        ServiceOrderCustomerOutput output = new ServiceOrderCustomerOutput(order, vehicle, customer);
        return serviceOrderPresenter.toCustomerView(output);
    }
}
