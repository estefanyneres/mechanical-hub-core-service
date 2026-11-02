package com.fiap.mechanical_hub.core_service.domain.entities;

import com.fiap.mechanical_hub.core_service.domain.enums.DocumentTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.fiap.mechanical_hub.core_service.shared.utils.Formatter.removeFormatting;
import static com.fiap.mechanical_hub.core_service.shared.utils.document.DocumentValidator.validateDocument;
import static com.fiap.mechanical_hub.core_service.shared.utils.telephone.TelephoneValidator.validateTelephone;

public class Customer {

    private UUID id;
    private String name;
    private DocumentTypeEnum documentTypeEnum;
    private String documentNumber;
    private String telephone;
    private String email;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Customer() {}

    public Customer(UUID id, String name, DocumentTypeEnum documentTypeEnum, String documentNumber,
                    String telephone, String email, String address,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.documentTypeEnum = documentTypeEnum;
        this.documentNumber = documentNumber;
        this.telephone = telephone;
        this.email = email;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Customer create(String name, DocumentTypeEnum documentTypeEnum, String documentNumber,
                                  String telephone, String email, String address) {
        validateDocument(documentTypeEnum, documentNumber);
        validateTelephone(telephone);

        Customer customer = new Customer();
        customer.id = UUID.randomUUID();
        customer.name = name;
        customer.documentTypeEnum = documentTypeEnum;
        customer.documentNumber = removeFormatting(documentNumber);
        customer.telephone = removeFormatting(telephone);
        customer.email = email;
        customer.address = address;
        customer.createdAt = LocalDateTime.now();
        customer.updatedAt = LocalDateTime.now();

        return customer;
    }

    public void update(String name, DocumentTypeEnum documentTypeEnum, String documentNumber,
                       String telephone, String email, String address) {
        validateDocument(documentTypeEnum, documentNumber);
        validateTelephone(telephone);

        this.name = name;
        this.documentTypeEnum = documentTypeEnum;
        this.documentNumber = removeFormatting(documentNumber);
        this.telephone = removeFormatting(telephone);
        this.email = email;
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public DocumentTypeEnum getDocumentTypeEnum() { return documentTypeEnum; }
    public String getDocumentNumber() { return documentNumber; }
    public String getTelephone() { return telephone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
