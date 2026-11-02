package com.fiap.mechanical_hub.core_service.infrastructure.http.controllers;

import com.fiap.mechanical_hub.core_service.application.controllers.ServiceOrderController;
import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.*;
import com.fiap.mechanical_hub.core_service.application.gateways.*;
import com.fiap.mechanical_hub.core_service.application.mappers.CustomerMapper;
import com.fiap.mechanical_hub.core_service.application.mappers.VehicleMapper;
import com.fiap.mechanical_hub.core_service.application.presenters.*;
import com.fiap.mechanical_hub.core_service.application.usecases.SagaOrchestratorUseCase;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.OrderStatusTransitionFactory;
import com.fiap.mechanical_hub.core_service.infrastructure.security.UserSecurityAdapter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Transactional
@RestController
@RequestMapping("/service-orders")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Ordens de Serviço", description = "Endpoints para gerenciamento de ordens de serviço")
public class ServiceOrderResource {

    private final ServiceOrderController controller;

    public ServiceOrderResource(
            ServiceOrderGateway serviceOrderGateway,
            CustomerGateway customerGateway, CustomerMapper customerMapper, CustomerPresenter customerPresenter,
            VehicleGateway vehicleGateway, VehicleMapper vehicleMapper, VehiclePresenter vehiclePresenter,
            OrderStatusTransitionFactory factory, ServiceOrderPresenter serviceOrderPresenter,
            SagaOrchestratorUseCase sagaOrchestratorUseCase,
            ServiceOrderStatusHistoryGateway statusHistoryGateway, MeterRegistry registry) {
        this.controller = new ServiceOrderController(serviceOrderGateway,
                customerGateway, customerMapper, customerPresenter,
                vehicleGateway, vehicleMapper, vehiclePresenter,
                factory, serviceOrderPresenter, sagaOrchestratorUseCase,
                statusHistoryGateway, registry);
    }

    @PostMapping
    @Operation(summary = "Criar nova ordem de serviço",
            description = "Cria uma nova ordem de serviço para um cliente. Não dispara o Saga ainda - isso só acontece quando o status for atualizado pra 'aguardando aprovação'. Requer autenticação.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ordem criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Cliente ou veículo não encontrado")
    })
    public ResponseEntity<ServiceOrderResponse> create(
            @RequestBody CreateServiceOrderRequest request,
            @AuthenticationPrincipal UserSecurityAdapter userDetails) {
        UUID userId = userDetails.user().getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(controller.create(request, userId));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status da ordem de serviço",
            description = "Atualiza o status de uma ordem de serviço (transições manuais do funcionário: iniciar diagnóstico, etc). Ao mover pra 'aguardando aprovação', dispara o Saga (publica orcamento.solicitado). Requer autenticação.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Transição de status inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Ordem não encontrada")
    })
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal UserSecurityAdapter userDetails) {
        UUID userId = userDetails.user().getId();
        controller.updateOrderStatus(id, request.status(), userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar todas as ordens de serviço",
            description = "Retorna lista de todas as ordens de serviço. Requer perfil de Administrador.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de ordens"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)")
    })
    public ResponseEntity<List<ServiceOrderSummaryResponse>> findAll() {
        return ResponseEntity.ok(controller.findAll());
    }

    @GetMapping("/active")
    @Operation(summary = "Listar ordens ativas por prioridade",
            description = "Retorna ordens ativas ordenadas por status (Em Execução > Aguardando Aprovação > Aprovado > Em Diagnóstico > Recebido), mais antigas primeiro. Exclui FINALIZADO, ENTREGUE e RECUSADO.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de ordens ativas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<List<ServiceOrderSummaryResponse>> findActive() {
        return ResponseEntity.ok(controller.findActiveSortedByPriority());
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Consultar status de uma ordem de serviço",
            description = "Retorna o status atual de uma ordem de serviço. Requer autenticação.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status da ordem"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Ordem não encontrada")
    })
    public ResponseEntity<Map<String, String>> findStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("status", controller.findStatusById(id)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter detalhes de uma ordem de serviço",
            description = "Retorna informações detalhadas de uma ordem específica. Requer perfil de Administrador.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhes da ordem"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)"),
            @ApiResponse(responseCode = "404", description = "Ordem não encontrada")
    })
    public ResponseEntity<ServiceOrderDetailResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(controller.findById(id));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Consultar histórico de status de uma ordem de serviço",
            description = "Retorna todas as transições de status já registradas pra uma ordem, em ordem cronológica. Requer autenticação.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico de status"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Ordem não encontrada")
    })
    public ResponseEntity<List<ServiceOrderStatusHistoryResponse>> findHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(controller.findHistoryById(id));
    }
}
