package com.fiap.mechanical_hub.core_service.infrastructure.messaging;

import com.fiap.mechanical_hub.core_service.application.dto.saga.ExecucaoRevertidaEvent;
import com.fiap.mechanical_hub.core_service.application.dto.saga.OrcamentoSolicitadoEvent;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SagaEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public SagaEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrcamentoSolicitado(ServiceOrder order) {
        OrcamentoSolicitadoEvent event = new OrcamentoSolicitadoEvent(order.getId(), order.getOrderNumber(), order.getRequestDescription());
        rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EXCHANGE, RabbitMQConfig.ROUTING_KEY_ORCAMENTO_SOLICITADO, event);
    }

    public void publishExecucaoRevertida(UUID serviceOrderId) {
        ExecucaoRevertidaEvent event = new ExecucaoRevertidaEvent(serviceOrderId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EXCHANGE, RabbitMQConfig.ROUTING_KEY_EXECUCAO_REVERTIDA, event);
    }
}
