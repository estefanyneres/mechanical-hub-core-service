package com.fiap.mechanical_hub.core_service.bdd.fakes;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Fake em memoria pro cenario BDD (TASK-039) - sem banco real, mesma
 * filosofia dos testes unitarios do projeto.
 */
public class InMemoryServiceOrderGateway implements ServiceOrderGateway {

    private final Map<UUID, ServiceOrder> store = new LinkedHashMap<>();

    @Override
    public ServiceOrder save(ServiceOrder serviceOrder) {
        store.put(serviceOrder.getId(), serviceOrder);
        return serviceOrder;
    }

    @Override
    public Optional<String> findLastOrderNumberByYearMonth(String yearMonth) {
        return store.values().stream()
                .map(ServiceOrder::getOrderNumber)
                .filter(n -> n.contains(yearMonth))
                .max(Comparator.naturalOrder());
    }

    @Override
    public Optional<ServiceOrder> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<ServiceOrder> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(UUID id) {
        store.remove(id);
    }

    @Override
    public List<ServiceOrder> findAllByOrderByCreatedAtDesc() {
        return store.values().stream()
                .sorted(Comparator.comparing(ServiceOrder::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ServiceOrder> findByOrderNumber(String orderNumber) {
        return store.values().stream()
                .filter(o -> o.getOrderNumber().equals(orderNumber))
                .findFirst();
    }

    @Override
    public List<ServiceOrder> findActiveOrdersSortedByPriority() {
        return new ArrayList<>(store.values());
    }
}
