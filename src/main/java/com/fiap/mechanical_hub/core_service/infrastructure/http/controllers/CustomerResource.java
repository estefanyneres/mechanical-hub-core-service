package com.fiap.mechanical_hub.core_service.infrastructure.http.controllers;

import com.fiap.mechanical_hub.core_service.application.controllers.CustomerController;
import com.fiap.mechanical_hub.core_service.application.dto.customer.CustomerResponse;
import com.fiap.mechanical_hub.core_service.application.dto.customer.UpsertCustomerRequest;
import com.fiap.mechanical_hub.core_service.application.gateways.CustomerGateway;
import com.fiap.mechanical_hub.core_service.application.mappers.CustomerMapper;
import com.fiap.mechanical_hub.core_service.application.presenters.CustomerPresenter;
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
@RequestMapping("/customers")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Clientes", description = "Endpoints para gerenciamento de clientes")
public class CustomerResource {

    private final CustomerController controller;

    public CustomerResource(CustomerGateway customerGateway, CustomerMapper customerMapper,
                            CustomerPresenter customerPresenter) {
        this.controller = new CustomerController(customerGateway, customerMapper, customerPresenter);
    }

    @PostMapping
    @Operation(summary = "Criar novo cliente", description = "Cria um novo cliente. Requer perfil de Administrador.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)")
    })
    public ResponseEntity<CustomerResponse> create(@RequestBody UpsertCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(controller.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar todos os clientes", description = "Retorna lista de todos os clientes. Requer autenticação.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)")
    })
    public ResponseEntity<List<CustomerResponse>> findAll() {
        return ResponseEntity.ok(controller.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter cliente por ID", description = "Retorna os detalhes de um cliente específico. Requer autenticação.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<CustomerResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(controller.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente. Requer perfil de Administrador.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<CustomerResponse> update(@PathVariable UUID id, @RequestBody UpsertCustomerRequest request) {
        return ResponseEntity.ok(controller.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar cliente", description = "Deleta um cliente do sistema. Requer perfil de Administrador.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente deletado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        controller.delete(id);
        return ResponseEntity.noContent().build();
    }
}
