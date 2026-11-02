package com.fiap.mechanical_hub.core_service.infrastructure.http.controllers;

import com.fiap.mechanical_hub.core_service.application.controllers.VehicleController;
import com.fiap.mechanical_hub.core_service.application.dto.vehicle.UpsertVehicleRequest;
import com.fiap.mechanical_hub.core_service.application.dto.vehicle.VehicleResponse;
import com.fiap.mechanical_hub.core_service.application.gateways.CustomerGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.VehicleGateway;
import com.fiap.mechanical_hub.core_service.application.mappers.VehicleMapper;
import com.fiap.mechanical_hub.core_service.application.presenters.VehiclePresenter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Transactional
@RestController
@RequestMapping("/vehicles")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Veículos", description = "Endpoints para gerenciamento de veículos de clientes")
public class VehicleResource {

    private final VehicleController controller;

    public VehicleResource(VehicleGateway vehicleGateway, CustomerGateway customerGateway,
                           VehicleMapper vehicleMapper, VehiclePresenter vehiclePresenter) {
        this.controller = new VehicleController(vehicleGateway, customerGateway, vehicleMapper, vehiclePresenter);
    }

    @PostMapping
    @Operation(summary = "Criar novo veículo", description = "Registra um novo veículo para um cliente. Requer perfil de Administrador.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Veículo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<VehicleResponse> create(
            @RequestParam("customer_id") UUID customerId,
            @RequestBody UpsertVehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(controller.create(customerId, request));
    }

    @GetMapping
    @Operation(summary = "Listar todos os veículos", description = "Retorna lista de todos os veículos cadastrados. Requer autenticação.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de veículos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)")
    })
    public ResponseEntity<List<VehicleResponse>> findAll() {
        return ResponseEntity.ok(controller.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter veículo por ID", description = "Retorna os detalhes de um veículo específico. Requer autenticação.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Veículo encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)"),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    public ResponseEntity<VehicleResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(controller.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar veículo", description = "Atualiza os dados de um veículo. Requer perfil de Administrador.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Veículo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)"),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    public ResponseEntity<VehicleResponse> update(@PathVariable UUID id, @RequestBody UpsertVehicleRequest request) {
        return ResponseEntity.ok(controller.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar veículo", description = "Deleta um veículo do sistema. Requer perfil de Administrador.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Veículo deletado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)"),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        controller.delete(id);
        return ResponseEntity.noContent().build();
    }
}
