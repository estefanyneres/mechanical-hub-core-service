package com.fiap.mechanical_hub.core_service.infrastructure.database.repositories;

import com.fiap.mechanical_hub.core_service.infrastructure.database.models.ServiceOrderStatusHistoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceOrderStatusHistoryJpaRepository extends JpaRepository<ServiceOrderStatusHistoryModel, UUID> {

    List<ServiceOrderStatusHistoryModel> findByServiceOrderIdOrderByChangedAtAsc(UUID serviceOrderId);

    Optional<ServiceOrderStatusHistoryModel> findFirstByServiceOrderIdOrderByChangedAtDesc(UUID serviceOrderId);
}
