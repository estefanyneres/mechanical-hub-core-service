package com.fiap.mechanical_hub.core_service.infrastructure.database.models;

import com.fiap.mechanical_hub.core_service.domain.enums.SagaStepEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "saga_state", indexes = {
        @Index(name = "idx_saga_state_service_order_id", columnList = "service_order_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SagaStateModel {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "service_order_id", nullable = false)
    private UUID serviceOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa_atual", nullable = false)
    private SagaStepEnum etapaAtual;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
