package com.fiap.mechanical_hub.core_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import com.fiap.mechanical_hub.core_service.domain.entities.mocks.CustomerMock;
import com.fiap.mechanical_hub.core_service.infrastructure.database.models.CustomerModel;
import com.fiap.mechanical_hub.core_service.infrastructure.database.repositories.CustomerJpaRepository;
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
class CustomerGatewayImplTest {

    @Mock
    private CustomerJpaRepository jpaRepository;

    @InjectMocks
    private CustomerGatewayImpl gateway;

    @Test
    void shouldSaveCustomer() {
        Customer customer = CustomerMock.defaultCustomer();
        when(jpaRepository.save(any(CustomerModel.class))).thenAnswer(i -> i.getArgument(0));

        Customer result = gateway.save(customer);

        assertEquals(customer.getId(), result.getId());
        assertEquals(customer.getDocumentNumber(), result.getDocumentNumber());
    }

    @Test
    void shouldFindById() {
        Customer customer = CustomerMock.defaultCustomer();
        when(jpaRepository.findById(customer.getId())).thenReturn(Optional.of(toModel(customer)));

        Optional<Customer> result = gateway.findById(customer.getId());

        assertTrue(result.isPresent());
        assertEquals(customer.getId(), result.get().getId());
    }

    @Test
    void shouldReturnEmptyWhenNotFoundById() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        assertTrue(gateway.findById(id).isEmpty());
    }

    @Test
    void shouldFindByDocumentNumber() {
        Customer customer = CustomerMock.defaultCustomer();
        when(jpaRepository.findByDocumentNumber(customer.getDocumentNumber())).thenReturn(Optional.of(toModel(customer)));

        assertTrue(gateway.findByDocumentNumber(customer.getDocumentNumber()).isPresent());
    }

    @Test
    void shouldFindAll() {
        Customer customer = CustomerMock.defaultCustomer();
        when(jpaRepository.findAll()).thenReturn(List.of(toModel(customer)));

        List<Customer> result = gateway.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void shouldDeleteById() {
        UUID id = UUID.randomUUID();

        gateway.deleteById(id);

        verify(jpaRepository).deleteById(id);
    }

    @Test
    void shouldCheckExistsByDocumentNumber() {
        when(jpaRepository.existsByDocumentNumber("52998224725")).thenReturn(true);

        assertTrue(gateway.existsByDocumentNumber("52998224725"));
    }

    @Test
    void shouldCheckExistsByDocumentNumberAndIdNot() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.existsByDocumentNumberAndIdNot("52998224725", id)).thenReturn(false);

        assertFalse(gateway.existsByDocumentNumberAndIdNot("52998224725", id));
    }

    private CustomerModel toModel(Customer customer) {
        return new CustomerModel(customer.getId(), customer.getName(), customer.getDocumentTypeEnum(),
                customer.getDocumentNumber(), customer.getTelephone(), customer.getEmail(), customer.getAddress(),
                customer.getCreatedAt(), customer.getUpdatedAt());
    }
}
