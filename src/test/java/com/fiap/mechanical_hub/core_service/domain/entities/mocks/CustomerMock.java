package com.fiap.mechanical_hub.core_service.domain.entities.mocks;

import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import com.fiap.mechanical_hub.core_service.domain.enums.DocumentTypeEnum;

import static com.fiap.mechanical_hub.core_service.domain.entities.constants.TestConstants.*;

public class CustomerMock {

    public static Customer defaultCustomer() {
        return Customer.create(
                DEFAULT_CUSTOMER_NAME,
                DocumentTypeEnum.CPF,
                DEFAULT_CUSTOMER_DOCUMENT,
                DEFAULT_CUSTOMER_TELEPHONE,
                DEFAULT_CUSTOMER_EMAIL,
                DEFAULT_CUSTOMER_ADDRESS
        );
    }

    public static Customer customerWithCNPJ() {
        return Customer.create(
                "Empresa ABC Ltda",
                DocumentTypeEnum.CNPJ,
                "11222333000181",
                DEFAULT_CUSTOMER_TELEPHONE,
                "empresa@example.com",
                DEFAULT_CUSTOMER_ADDRESS
        );
    }

    public static Customer customerWithCustomValues(String name, String documentNumber, String telephone, String email, String address) {
        return Customer.create(name, DocumentTypeEnum.CPF, documentNumber, telephone, email, address);
    }
}
