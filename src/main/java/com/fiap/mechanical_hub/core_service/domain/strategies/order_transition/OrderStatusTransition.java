package com.fiap.mechanical_hub.core_service.domain.strategies.order_transition;

import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;

import java.util.UUID;

public interface OrderStatusTransition {
    void execute(ServiceOrder order, UUID userId);
}
