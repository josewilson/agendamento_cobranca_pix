package org.example.agendamento.domain.model.agendamento;

import java.util.Set;

public enum StatusAgendamento {
    PENDENTE_PAGAMENTO,
    CONFIRMADO,
    EM_ANDAMENTO,
    CONCLUIDO,
    CANCELADO,
    EXPIRADO,
    NO_SHOW;

    public boolean podeTransicionarPara(StatusAgendamento novoStatus) {
        return transicoesValidas().contains(novoStatus);
    }

    public boolean isTerminal() {
        return transicoesValidas().isEmpty();
    }

    private Set<StatusAgendamento> transicoesValidas() {
        return switch (this) {
            case PENDENTE_PAGAMENTO -> Set.of(CONFIRMADO, CANCELADO, EXPIRADO);
            case CONFIRMADO -> Set.of(EM_ANDAMENTO, CANCELADO, NO_SHOW);
            case EM_ANDAMENTO -> Set.of(CONCLUIDO);
            case CONCLUIDO, CANCELADO, EXPIRADO, NO_SHOW -> Set.of();
        };
    }
}
