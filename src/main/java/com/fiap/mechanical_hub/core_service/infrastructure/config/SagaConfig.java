package com.fiap.mechanical_hub.core_service.infrastructure.config;

import com.fiap.mechanical_hub.core_service.application.gateways.SagaStateGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderGateway;
import com.fiap.mechanical_hub.core_service.application.gateways.ServiceOrderStatusHistoryGateway;
import com.fiap.mechanical_hub.core_service.application.usecases.SagaOrchestratorUseCase;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.OrderStatusTransitionFactory;
import com.fiap.mechanical_hub.core_service.domain.strategies.order_transition.RevertToDiagnosisTransition;
import com.fiap.mechanical_hub.core_service.infrastructure.messaging.SagaEventPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expoe o SagaOrchestratorUseCase como bean (em vez de instanciado na
 * mao dentro de um Resource, como os demais usecases) porque ele precisa
 * ser a MESMA instancia tanto pro SagaEventListener (RabbitMQ) quanto pro
 * ServiceOrderController (REST, ao criar a OS) -- nao dois orquestradores
 * separados.
 */
@Configuration
public class SagaConfig {

    @Bean
    public SagaOrchestratorUseCase sagaOrchestratorUseCase(
            SagaStateGateway sagaStateGateway,
            ServiceOrderGateway serviceOrderGateway,
            OrderStatusTransitionFactory factory,
            SagaEventPublisher publisher,
            ServiceOrderStatusHistoryGateway statusHistoryGateway,
            MeterRegistry registry) {
        return new SagaOrchestratorUseCase(sagaStateGateway, serviceOrderGateway, factory, publisher,
                new RevertToDiagnosisTransition(statusHistoryGateway, registry));
    }
}
