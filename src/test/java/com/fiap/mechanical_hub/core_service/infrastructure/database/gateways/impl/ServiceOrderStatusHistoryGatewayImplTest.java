package com.fiap.mechanical_hub.core_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrderStatusHistory;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import com.fiap.mechanical_hub.core_service.infrastructure.database.mappers.ServiceOrderStatusHistoryJpaMapper;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.ServiceOrderStatusHistoryModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.repositories.ServiceOrderStatusHistoryJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOrderStatusHistoryGatewayImplTest {

    @Mock
    private ServiceOrderStatusHistoryJpaRepository jpaRepository;

    @InjectMocks
    private ServiceOrderStatusHistoryGatewayImpl gateway;

    @Test
    void shouldSave() {
        ServiceOrderStatusHistory history = ServiceOrderStatusHistory.register(
                UUID.randomUUID(), OrderStatusEnum.RECEBIDO, OrderStatusEnum.EM_DIAGNOSTICO, UUID.randomUUID());
        when(jpaRepository.save(any(ServiceOrderStatusHistoryModel.class)))
                .thenAnswer(i -> i.getArgument(0));

        ServiceOrderStatusHistory result = gateway.save(history);

        assertEquals(history.getServiceOrderId(), result.getServiceOrderId());
    }

    @Test
    void shouldFindByServiceOrderId() {
        UUID orderId = UUID.randomUUID();
        ServiceOrderStatusHistory history = ServiceOrderStatusHistory.register(
                orderId, OrderStatusEnum.RECEBIDO, OrderStatusEnum.EM_DIAGNOSTICO, UUID.randomUUID());
        when(jpaRepository.findByServiceOrderIdOrderByChangedAtAsc(orderId))
                .thenReturn(List.of(ServiceOrderStatusHistoryJpaMapper.toJpa(history)));

        List<ServiceOrderStatusHistory> result = gateway.findByServiceOrderId(orderId);

        assertEquals(1, result.size());
    }

    @Test
    void shouldFindLastByServiceOrderId() {
        UUID orderId = UUID.randomUUID();
        ServiceOrderStatusHistory history = ServiceOrderStatusHistory.register(
                orderId, OrderStatusEnum.RECEBIDO, OrderStatusEnum.EM_DIAGNOSTICO, UUID.randomUUID());
        when(jpaRepository.findFirstByServiceOrderIdOrderByChangedAtDesc(orderId))
                .thenReturn(Optional.of(ServiceOrderStatusHistoryJpaMapper.toJpa(history)));

        Optional<ServiceOrderStatusHistory> result = gateway.findLastByServiceOrderId(orderId);

        assertTrue(result.isPresent());
    }

    @Test
    void shouldReturnEmptyWhenNoLastHistoryExists() {
        UUID orderId = UUID.randomUUID();
        when(jpaRepository.findFirstByServiceOrderIdOrderByChangedAtDesc(orderId)).thenReturn(Optional.empty());

        assertTrue(gateway.findLastByServiceOrderId(orderId).isEmpty());
    }
}
