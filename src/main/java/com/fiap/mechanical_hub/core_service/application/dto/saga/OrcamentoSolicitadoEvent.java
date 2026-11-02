package com.fiap.mechanical_hub.core_service.application.dto.saga;

import java.util.UUID;

/**
 * Publicado pelo core-service quando o mecanico move a OS pra
 * AGUARDANDO_APROVACAO (routing key "orcamento.solicitado"). Consumido
 * pelo billing-service (TASK-033) pra gerar o orcamento.
 */
public record OrcamentoSolicitadoEvent(UUID serviceOrderId, String orderNumber, String requestDescription) {}
