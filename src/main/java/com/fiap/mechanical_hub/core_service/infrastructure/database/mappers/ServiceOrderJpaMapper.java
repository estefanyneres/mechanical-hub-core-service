package com.fiap.mechanical_hub.core_service.infrastructure.database.mappers;

import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.ServiceOrderModel;

public class ServiceOrderJpaMapper {

    private ServiceOrderJpaMapper() {}

    public static ServiceOrderModel toJpa(ServiceOrder order) {
        return new ServiceOrderModel(
                order.getId(),
                order.getVehicleId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getCreatedByUserId(),
                order.getResponsibleUserId(),
                order.getOrderNumber(),
                order.getRequestDescription(),
                order.getBudget(),
                order.getEstimatedCompletionAt(),
                order.getOpenedAt(),
                order.getCompletedAt(),
                order.getDeliveredAt(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public static ServiceOrder toDomain(ServiceOrderModel entity) {
        return new ServiceOrder(
                entity.getId(),
                entity.getVehicleId(),
                entity.getCustomerId(),
                entity.getOrderStatusEnum(),
                entity.getCreatedByUserId(),
                entity.getResponsibleUserId(),
                entity.getOrderNumber(),
                entity.getRequestDescription(),
                entity.getBudget(),
                entity.getEstimatedCompletionAt(),
                entity.getOpenedAt(),
                entity.getCompletedAt(),
                entity.getDeliveredAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
