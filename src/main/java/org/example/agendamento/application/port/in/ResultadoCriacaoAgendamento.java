package org.example.agendamento.application.port.in;

import org.example.agendamento.application.port.out.CobrancaPix;
import org.example.agendamento.domain.model.agendamento.Agendamento;

import java.util.Optional;

public record ResultadoCriacaoAgendamento(Agendamento agendamento, Optional<CobrancaPix> cobranca) {
}
