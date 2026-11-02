package com.fiap.mechanical_hub.core_service.application.usecases;

import com.fiap.mechanical_hub.core_service.application.dto.customer.CustomerResponse;
import com.fiap.mechanical_hub.core_service.application.dto.customer.UpsertCustomerRequest;
import com.fiap.mechanical_hub.core_service.application.gateways.CustomerGateway;
import com.fiap.mechanical_hub.core_service.application.mappers.CustomerMapper;
import com.fiap.mechanical_hub.core_service.application.presenters.CustomerPresenter;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import com.fiap.mechanical_hub.core_service.domain.enums.DocumentTypeEnum;
import com.fiap.mechanical_hub.core_service.domain.exceptions.DuplicateDocumentException;
import com.fiap.mechanical_hub.core_service.domain.exceptions.InvalidDocumentException;
import com.fiap.mechanical_hub.core_service.domain.exceptions.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.fiap.mechanical_hub.core_service.shared.utils.Formatter.removeFormatting;

public class CustomerUseCase {

    public static final String CLIENTE_NAO_ENCONTRADO_PARA_O_ID = "Cliente não encontrado para o id: ";
    private final CustomerGateway customerGateway;
    private final CustomerMapper customerMapper;
    private final CustomerPresenter customerPresenter;

    public CustomerUseCase(CustomerGateway customerGateway, CustomerMapper customerMapper,
                           CustomerPresenter customerPresenter) {
        this.customerGateway = customerGateway;
        this.customerMapper = customerMapper;
        this.customerPresenter = customerPresenter;
    }

    public CustomerResponse create(UpsertCustomerRequest request) {
        String cleanDocumentNumber = removeFormatting(request.documentNumber());

        if (customerGateway.existsByDocumentNumber(cleanDocumentNumber)) {
            throw new DuplicateDocumentException(
                    String.format("Cliente com documento %s já existe", request.documentNumber())
            );
        }

        Customer customer = customerMapper.toDomainEntity(request);
        Customer saved = customerGateway.save(customer);
        return customerPresenter.toResponse(saved);
    }

    public Customer findById(UUID id) {
        return customerGateway.findById(id)
                .orElseThrow(() -> new NotFoundException(CLIENTE_NAO_ENCONTRADO_PARA_O_ID + id));
    }

    public CustomerResponse findByIdAndPresent(UUID id) {
        return customerPresenter.toResponse(findById(id));
    }

    public List<CustomerResponse> findAll() {
        return customerPresenter.toResponseList(customerGateway.findAll());
    }

    public CustomerResponse update(UUID id, UpsertCustomerRequest request) {
        Customer existingCustomer = customerGateway.findById(id)
                .orElseThrow(() -> new NotFoundException(CLIENTE_NAO_ENCONTRADO_PARA_O_ID + id));

        String cleanRequestDocument = removeFormatting(request.documentNumber());
        String cleanExistingDocument = existingCustomer.getDocumentNumber();

        if (!cleanExistingDocument.equals(cleanRequestDocument)) {
            throw new InvalidDocumentException("Não é permitido alterar o documento do cliente");
        }

        if (customerGateway.existsByDocumentNumberAndIdNot(cleanRequestDocument, id)) {
            throw new DuplicateDocumentException(
                    String.format("Cliente com documento %s já existe", request.documentNumber()));
        }

        Customer updatedCustomer = customerMapper.toDomainEntity(request, existingCustomer);
        Customer saved = customerGateway.save(updatedCustomer);
        return customerPresenter.toResponse(saved);
    }

    public void delete(UUID id) {
        if (customerGateway.findById(id).isEmpty()) {
            throw new NotFoundException(CLIENTE_NAO_ENCONTRADO_PARA_O_ID + id);
        }
        customerGateway.deleteById(id);
    }

    public Customer findByDocumentOrCreate(String name, String documentType, String documentNumber,
                                           String telephone, String email, String address) {
        String cleanDocumentNumber = removeFormatting(documentNumber);

        Optional<Customer> existingCustomer = customerGateway.findByDocumentNumber(cleanDocumentNumber);
        if (existingCustomer.isPresent()) {
            return existingCustomer.get();
        }

        DocumentTypeEnum type = DocumentTypeEnum.fromValue(documentType);
        Customer newCustomer = Customer.create(name, type, documentNumber, telephone, email, address);
        return customerGateway.save(newCustomer);
    }
}
