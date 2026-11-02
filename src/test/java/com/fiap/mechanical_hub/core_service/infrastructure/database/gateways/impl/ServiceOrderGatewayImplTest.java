package com.fiap.mechanical_hub.core_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.entities.mocks.ServiceOrderMock;
import com.fiap.mechanical_hub.core_service.infrastructure.database.mappers.ServiceOrderJpaMapper;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.ServiceOrderModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.repositories.ServiceOrderJpaRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOrderGatewayImplTest {

    @Mock
    private ServiceOrderJpaRepository jpaRepository;

    @InjectMocks
    private ServiceOrderGatewayImpl gateway;

    @Test
    void shouldSave() {
        ServiceOrder order = ServiceOrderMock.defaultServiceOrder();
        when(jpaRepository.save(any(ServiceOrderModel.class))).thenAnswer(i -> i.getArgument(0));

        ServiceOrder result = gateway.save(order);

        assertEquals(order.getOrderNumber(), result.getOrderNumber());
    }

    @Test
    void shouldFindLastOrderNumberByYearMonth() {
        when(jpaRepository.findLastOrderNumberByYearMonth("202601")).thenReturn(Optional.of("OS-202601-0001"));

        assertEquals(Optional.of("OS-202601-0001"), gateway.findLastOrderNumberByYearMonth("202601"));
    }

    @Test
    void shouldFindById() {
        ServiceOrder order = ServiceOrderMock.defaultServiceOrder();
        when(jpaRepository.findById(order.getId())).thenReturn(Optional.of(ServiceOrderJpaMapper.toJpa(order)));

        assertTrue(gateway.findById(order.getId()).isPresent());
    }

    @Test
    void shouldFindAll() {
        ServiceOrder order = ServiceOrderMock.defaultServiceOrder();
        when(jpaRepository.findAll()).thenReturn(List.of(ServiceOrderJpaMapper.toJpa(order)));

        assertEquals(1, gateway.findAll().size());
    }

    @Test
    void shouldDeleteById() {
        UUID id = UUID.randomUUID();

        gateway.deleteById(id);

        verify(jpaRepository).deleteById(id);
    }

    @Test
    void shouldFindAllByOrderByCreatedAtDesc() {
        ServiceOrder order = ServiceOrderMock.defaultServiceOrder();
        when(jpaRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(ServiceOrderJpaMapper.toJpa(order)));

        assertEquals(1, gateway.findAllByOrderByCreatedAtDesc().size());
    }

    @Test
    void shouldFindByOrderNumber() {
        ServiceOrder order = ServiceOrderMock.defaultServiceOrder();
        when(jpaRepository.findByOrderNumber(order.getOrderNumber())).thenReturn(Optional.of(ServiceOrderJpaMapper.toJpa(order)));

        assertTrue(gateway.findByOrderNumber(order.getOrderNumber()).isPresent());
    }

    @Test
    void shouldFindActiveOrdersSortedByPriority() {
        ServiceOrder order = ServiceOrderMock.defaultServiceOrder();
        when(jpaRepository.findActiveOrdersSortedByPriority()).thenReturn(List.of(ServiceOrderJpaMapper.toJpa(order)));

        assertEquals(1, gateway.findActiveOrdersSortedByPriority().size());
    }
}
