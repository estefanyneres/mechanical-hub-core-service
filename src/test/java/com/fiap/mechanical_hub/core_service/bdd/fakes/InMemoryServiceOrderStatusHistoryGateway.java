package com.fiap.mechanical_hub.core_service.bdd.fakes;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrderStatusHistory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryServiceOrderStatusHistoryGateway implements ServiceOrderStatusHistoryGateway {

    private final List<ServiceOrderStatusHistory> store = new ArrayList<>();

    @Override
    public ServiceOrderStatusHistory save(ServiceOrderStatusHistory history) {
        store.add(history);
        return history;
    }

    @Override
    public List<ServiceOrderStatusHistory> findByServiceOrderId(UUID serviceOrderId) {
        return store.stream()
                .filter(h -> h.getServiceOrderId().equals(serviceOrderId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ServiceOrderStatusHistory> findLastByServiceOrderId(UUID serviceOrderId) {
        return findByServiceOrderId(serviceOrderId).stream()
                .max(Comparator.comparing(ServiceOrderStatusHistory::getChangedAt));
    }
}
