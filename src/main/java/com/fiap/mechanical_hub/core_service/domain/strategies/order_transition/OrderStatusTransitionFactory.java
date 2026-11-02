package com.fiap.mechanical_hub.core_service.domain.strategies.order_transition;

import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;

import java.util.Map;

public class OrderStatusTransitionFactory {

    private final Map<OrderStatusEnum, OrderStatusTransition> transitions;

    public OrderStatusTransitionFactory(Map<OrderStatusEnum, OrderStatusTransition> transitions) {
        this.transitions = transitions;
    }

    public OrderStatusTransition get(OrderStatusEnum targetStatus) {
        OrderStatusTransition transition = transitions.get(targetStatus);

        if (transition == null) { throw new IllegalArgumentException("Transição de status não suportada"); }

        return transition;
    }

}
