package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.application.port.out.CobrancaPix;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;

import java.util.Optional;

public record ResultadoCriacaoAgendamento(Agendamento agendamento, Optional<CobrancaPix> cobranca) {
}
