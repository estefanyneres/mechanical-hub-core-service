package com.fiap.mechanical_hub.core_service.infrastructure.database.repositories;

import com.fiap.mechanical_hub.core_service.infrastructure.database.models.SagaStateModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SagaStateJpaRepository extends JpaRepository<SagaStateModel, UUID> {

    // Uma OS pode ter mais de uma tentativa de saga na vida (ex: execucao.falhou
    // manda de volta pra EM_DIAGNOSTICO, e uma nova aprovacao cria uma nova
    // linha). findByServiceOrderId (unico) quebraria com
    // IncorrectResultSizeDataAccessException a partir da 2a tentativa -- por
    // isso busca sempre a mais recente.
    Optional<SagaStateModel> findFirstByServiceOrderIdOrderByCreatedAtDesc(UUID serviceOrderId);
}
