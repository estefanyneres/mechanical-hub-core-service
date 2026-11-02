package com.fiap.mechanical_hub.core_service.application.mappers;

import com.fiap.mechanical_hub.core_service.application.dto.customer.UpsertCustomerRequest;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import com.fiap.mechanical_hub.core_service.domain.enums.DocumentTypeEnum;

public class CustomerMapper {

    public Customer toDomainEntity(UpsertCustomerRequest request) {
        DocumentTypeEnum documentTypeEnum = DocumentTypeEnum.fromValue(request.documentType());
        return Customer.create(
                request.name(),
                documentTypeEnum,
                request.documentNumber(),
                request.telephone(),
                request.email(),
                request.address()
        );
    }

    public Customer toDomainEntity(UpsertCustomerRequest request, Customer existingCustomer) {
        DocumentTypeEnum documentTypeEnum = DocumentTypeEnum.fromValue(request.documentType());
        existingCustomer.update(
                request.name(),
                documentTypeEnum,
                request.documentNumber(),
                request.telephone(),
                request.email(),
                request.address()
        );
        return existingCustomer;
    }
}
