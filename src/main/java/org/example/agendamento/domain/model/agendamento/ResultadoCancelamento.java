package org.example.agendamento.domain.model.agendamento;

import org.example.agendamento.domain.model.shared.Dinheiro;

public record ResultadoCancelamento(Dinheiro valorRetido, Dinheiro valorReembolsado) {
}
