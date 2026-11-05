package br.com.josewilson.agendamento.domain.model.agendamento;

import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;

public record ResultadoCancelamento(Dinheiro valorRetido, Dinheiro valorReembolsado) {
}
