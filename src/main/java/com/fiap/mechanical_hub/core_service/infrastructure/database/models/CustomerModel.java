package com.fiap.mechanical_hub.core_service.infrastructure.database.models;

import com.fiap.mechanical_hub.core_service.domain.enums.DocumentTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customers_document_number", columnList = "document_number"),
        @Index(name = "idx_customers_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerModel {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentTypeEnum documentTypeEnum;

    @Column(name = "document_number", nullable = false, unique = true)
    private String documentNumber;

    @Column(name = "telephone", nullable = false)
    private String telephone;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
