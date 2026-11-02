package com.fiap.mechanical_hub.core_service.application.gateways;

import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrderStatusHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderStatusHistoryGateway {

    ServiceOrderStatusHistory save(ServiceOrderStatusHistory history);

    List<ServiceOrderStatusHistory> findByServiceOrderId(UUID serviceOrderId);

    Optional<ServiceOrderStatusHistory> findLastByServiceOrderId(UUID serviceOrderId);
}
