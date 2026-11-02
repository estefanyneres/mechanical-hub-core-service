package com.fiap.mechanical_hub.core_service.domain.enums;

public enum OrderStatusEnum {
    RECEBIDO("Recebido", false),
    EM_DIAGNOSTICO("Em diagnóstico", true),
    AGUARDANDO_APROVACAO("Aguardando aprovação", true),
    APROVADO("Aprovado", false),
    RECUSADO("Recusado", false),
    // Fase 4 (TASK-016, redesenho pos-feedback do usuario): pagamento pode
    // falhar depois do orcamento aprovado -- antes nao havia pra onde ir.
    FALHA_PAGAMENTO("Falha no pagamento", false),
    // Pagamento confirmado != material disponivel nem mecanico disponivel.
    // Esses dois fatos chegam depois, via eventos separados.
    PAGAMENTO_REALIZADO("Pagamento realizado", false),
    PRONTO_PARA_EXECUCAO("Pronto para execução", false),
    PENDENTE_ESTOQUE("Pendente de estoque", false),
    // Manual de proposito: pagamento+material OK nao significa que o
    // mecanico tem disponibilidade imediata pra comecar (RFC-001).
    EM_EXECUCAO("Em execução", true),
    // Quem decide que a execucao terminou e o repair-service via evento
    // (execucao.finalizada), nao uma acao manual direta neste servico.
    FINALIZADO("Finalizado", false),
    ENTREGUE("Entregue", true);

    private final String displayName;
    private final boolean manuallySettable;

    OrderStatusEnum(String displayName, boolean manuallySettable) {
        this.displayName = displayName;
        this.manuallySettable = manuallySettable;
    }

    public String getDisplayName() { return displayName; }
    public boolean isManuallySettable() { return manuallySettable; }

    public static OrderStatusEnum fromString(String value) {
        for (OrderStatusEnum status : OrderStatusEnum.values()) {
            if (status.name().equals(value)) {
                return status;
            }
        }
        String validValues = java.util.Arrays.stream(OrderStatusEnum.values())
                .filter(OrderStatusEnum::isManuallySettable)
                .map(Enum::name)
                .collect(java.util.stream.Collectors.joining(", "));
        throw new IllegalArgumentException("Status de ordem inválido: '" + value + "'. Valores válidos: " + validValues);
    }
}
