package com.fiap.mechanical_hub.core_service.domain.enums;

/**
 * Etapas do Saga orquestrado (RFC-001). Estado de PROCESSO, separado do
 * OrderStatusEnum (estado de NEGOCIO da OS).
 */
public enum SagaStepEnum {
    AGUARDANDO_ORCAMENTO,
    AGUARDANDO_APROVACAO_ORCAMENTO,
    AGUARDANDO_PAGAMENTO,
    AGUARDANDO_EXECUCAO,
    CONCLUIDO,
    FALHOU
}
