package com.fiap.mechanical_hub.core_service.domain.strategies.order_transition;

import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrderStatusHistory;
import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServiceOrderStatusTransitionRecorderTest {

    @Test
    void shouldUseLastHistoryChangedAtWhenPresent() {
        ServiceOrderStatusHistoryGateway gateway = mock(ServiceOrderStatusHistoryGateway.class);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ServiceOrder order = mock(ServiceOrder.class);
        UUID orderId = UUID.randomUUID();
        LocalDateTime lastChangedAt = LocalDateTime.now().minusHours(3);

        ServiceOrderStatusHistory previous = mock(ServiceOrderStatusHistory.class);
        when(previous.getChangedAt()).thenReturn(lastChangedAt);
        when(order.getId()).thenReturn(orderId);
        when(gateway.findLastByServiceOrderId(orderId)).thenReturn(Optional.of(previous));

        ServiceOrderStatusTransitionRecorder.record(
                gateway, registry, order, OrderStatusEnum.EM_DIAGNOSTICO, OrderStatusEnum.AGUARDANDO_APROVACAO, null);

        verify(gateway).save(any(ServiceOrderStatusHistory.class));
        verify(order, never()).getCreatedAt();
        double totalHours = registry.get("service_order.status.duration")
                .tag("from_status", "EM_DIAGNOSTICO")
                .tag("to_status", "AGUARDANDO_APROVACAO")
                .timer().totalTime(TimeUnit.HOURS);
        assertEquals(3.0, totalHours, 0.1);
    }

    @Test
    void shouldFallBackToOrderCreatedAtWhenNoHistoryExists() {
        ServiceOrderStatusHistoryGateway gateway = mock(ServiceOrderStatusHistoryGateway.class);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ServiceOrder order = mock(ServiceOrder.class);
        UUID orderId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(30);

        when(order.getId()).thenReturn(orderId);
        when(order.getCreatedAt()).thenReturn(createdAt);
        when(gateway.findLastByServiceOrderId(orderId)).thenReturn(Optional.empty());

        ServiceOrderStatusTransitionRecorder.record(
                gateway, registry, order, OrderStatusEnum.RECEBIDO, OrderStatusEnum.EM_DIAGNOSTICO, null);

        verify(gateway).save(any(ServiceOrderStatusHistory.class));
        double totalMinutes = registry.get("service_order.status.duration")
                .tag("from_status", "RECEBIDO")
                .tag("to_status", "EM_DIAGNOSTICO")
                .timer().totalTime(TimeUnit.MINUTES);
        assertEquals(30.0, totalMinutes, 0.5);
    }

    @Test
    void shouldSaveHistoryWithChangedByUserId() {
        ServiceOrderStatusHistoryGateway gateway = mock(ServiceOrderStatusHistoryGateway.class);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ServiceOrder order = mock(ServiceOrder.class);
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(order.getId()).thenReturn(orderId);
        when(order.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(gateway.findLastByServiceOrderId(orderId)).thenReturn(Optional.empty());

        ServiceOrderStatusTransitionRecorder.record(
                gateway, registry, order, OrderStatusEnum.APROVADO, OrderStatusEnum.PAGAMENTO_REALIZADO, userId);

        ArgumentCaptor<ServiceOrderStatusHistory> captor = ArgumentCaptor.forClass(ServiceOrderStatusHistory.class);
        verify(gateway).save(captor.capture());
        assertEquals(userId, captor.getValue().getChangedByUserId());
        assertEquals(OrderStatusEnum.APROVADO, captor.getValue().getFromStatus());
        assertEquals(OrderStatusEnum.PAGAMENTO_REALIZADO, captor.getValue().getToStatus());
    }
}
