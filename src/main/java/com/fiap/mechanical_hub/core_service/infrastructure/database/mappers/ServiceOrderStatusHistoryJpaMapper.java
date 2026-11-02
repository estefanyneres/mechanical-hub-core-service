package com.fiap.mechanical_hub.core_service.infrastructure.database.mappers;

import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrderStatusHistory;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.ServiceOrderStatusHistoryModel;

public class ServiceOrderStatusHistoryJpaMapper {

    private ServiceOrderStatusHistoryJpaMapper() {}

    public static ServiceOrderStatusHistoryModel toJpa(ServiceOrderStatusHistory history) {
        return new ServiceOrderStatusHistoryModel(
                history.getId(),
                history.getServiceOrderId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getChangedByUserId(),
                history.getChangedAt()
        );
    }

    public static ServiceOrderStatusHistory toDomain(ServiceOrderStatusHistoryModel model) {
        return new ServiceOrderStatusHistory(
                model.getId(),
                model.getServiceOrderId(),
                model.getFromStatus(),
                model.getToStatus(),
                model.getChangedByUserId(),
                model.getChangedAt()
        );
    }
}
