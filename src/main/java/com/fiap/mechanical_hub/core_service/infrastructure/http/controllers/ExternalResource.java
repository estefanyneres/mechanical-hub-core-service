package com.fiap.mechanical_hub.core_service.infrastructure.http.controllers;

import com.fiap.mechanical_hub.core_service.application.controllers.ExternalController;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.ServiceOrderCustomerView;
import com.fiap.mechanical_hub.core_service.application.gateways.*;
import com.fiap.mechanical_hub.core_service.application.mappers.CustomerMapper;
import com.fiap.mechanical_hub.core_service.application.mappers.VehicleMapper;
import com.fiap.mechanical_hub.core_service.application.presenters.*;
import com.fiap.mechanical_hub.core_service.application.usecases.SagaOrchestratorUseCase;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.OrderStatusTransitionFactory;
import com.fiap.mechanical_hub.core_service.infrastructure.security.CustomerSecurityAdapter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * So a consulta pelo numero da OS continua aqui (TASK-015) -- aprovar e
 * recusar o orcamento agora acontece no billing-service, nao mais aqui
 * (ver ExternalController).
 */
@Transactional
@RestController
@RequestMapping("/mechanical-hub")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Consultas Externas", description = "Endpoints para clientes consultarem as próprias ordens de serviço")
public class ExternalResource {

    private final ExternalController controller;

    public ExternalResource(
            ServiceOrderGateway serviceOrderGateway,
            CustomerGateway customerGateway, CustomerMapper customerMapper, CustomerPresenter customerPresenter,
            VehicleGateway vehicleGateway, VehicleMapper vehicleMapper, VehiclePresenter vehiclePresenter,
            OrderStatusTransitionFactory factory, ServiceOrderPresenter serviceOrderPresenter,
            SagaOrchestratorUseCase sagaOrchestratorUseCase,
            ServiceOrderStatusHistoryGateway statusHistoryGateway, MeterRegistry registry) {
        this.controller = new ExternalController(serviceOrderGateway,
                customerGateway, customerMapper, customerPresenter,
                vehicleGateway, vehicleMapper, vehiclePresenter,
                factory, serviceOrderPresenter, sagaOrchestratorUseCase,
                statusHistoryGateway, registry);
    }

    @GetMapping("/service-orders/{orderNumber}")
    @Operation(summary = "Consultar ordem de serviço pelo número",
            description = "Retorna informações de uma ordem de serviço do próprio cliente autenticado (CPF + placa).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informações da ordem"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Ordem não encontrada ou não pertence ao cliente autenticado")
    })
    public ResponseEntity<ServiceOrderCustomerView> findByOrderNumber(
            @PathVariable String orderNumber,
            @AuthenticationPrincipal CustomerSecurityAdapter customerDetails) {
        return ResponseEntity.ok(controller.findByOrderNumber(orderNumber, customerDetails.customer().getId()));
    }
}
