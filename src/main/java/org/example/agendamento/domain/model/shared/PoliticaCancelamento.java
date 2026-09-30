package org.example.agendamento.domain.model.shared;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record PoliticaCancelamento(Duration antecedenciaMinima, BigDecimal percentualRetido) {

    public PoliticaCancelamento {
        Objects.requireNonNull(antecedenciaMinima, "antecedenciaMinima nao pode ser nula");
        Objects.requireNonNull(percentualRetido, "percentualRetido nao pode ser nulo");
        if (antecedenciaMinima.isNegative()) {
            throw new IllegalArgumentException("antecedenciaMinima nao pode ser negativa");
        }
        if (percentualRetido.compareTo(BigDecimal.ZERO) < 0 || percentualRetido.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("percentualRetido deve estar entre 0 e 100");
        }
    }

    public static PoliticaCancelamento padrao() {
        return new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100));
    }

    public boolean dentroDaJanelaLivre(Instant inicioAgendamento, Instant agora) {
        Objects.requireNonNull(inicioAgendamento, "inicioAgendamento nao pode ser nulo");
        Objects.requireNonNull(agora, "agora nao pode ser nulo");
        Duration antecedenciaReal = Duration.between(agora, inicioAgendamento);
        return !antecedenciaReal.isNegative() && antecedenciaReal.compareTo(antecedenciaMinima) >= 0;
    }
}
