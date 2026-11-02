package com.fiap.mechanical_hub.core_service.domain.entities;

import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ServiceOrderStatusHistoryTest {

    @Test
    void shouldCreateEmptyServiceOrderStatusHistory() {
        ServiceOrderStatusHistory history = new ServiceOrderStatusHistory();

        assertAll(
                () -> assertNull(history.getId()),
                () -> assertNull(history.getServiceOrderId()),
                () -> assertNull(history.getFromStatus()),
                () -> assertNull(history.getToStatus()),
                () -> assertNull(history.getChangedByUserId()),
                () -> assertNull(history.getChangedAt())
        );
    }

    @Test
    void shouldRegisterHistoryWithGeneratedIdAndTimestamp() {
        UUID serviceOrderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ServiceOrderStatusHistory history = ServiceOrderStatusHistory.register(
                serviceOrderId, OrderStatusEnum.AGUARDANDO_APROVACAO, OrderStatusEnum.APROVADO, userId);

        assertNotNull(history.getId());
        assertEquals(serviceOrderId, history.getServiceOrderId());
        assertEquals(OrderStatusEnum.AGUARDANDO_APROVACAO, history.getFromStatus());
        assertEquals(OrderStatusEnum.APROVADO, history.getToStatus());
        assertEquals(userId, history.getChangedByUserId());
        assertNotNull(history.getChangedAt());
    }

    @Test
    void shouldRegisterHistoryWithNullChangedByUserIdForAutomaticTransitions() {
        UUID serviceOrderId = UUID.randomUUID();

        ServiceOrderStatusHistory history = ServiceOrderStatusHistory.register(
                serviceOrderId, OrderStatusEnum.APROVADO, OrderStatusEnum.PAGAMENTO_REALIZADO, null);

        assertNotNull(history.getId());
        assertEquals(OrderStatusEnum.APROVADO, history.getFromStatus());
        assertEquals(OrderStatusEnum.PAGAMENTO_REALIZADO, history.getToStatus());
        assertNull(history.getChangedByUserId());
    }
}
