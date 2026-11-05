package br.com.josewilson.agendamento.domain.event;

import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;

import java.time.Instant;

public record AgendamentoConfirmado(AgendamentoId agendamentoId, ClienteId clienteId,
                                     Instant ocorridoEm) implements EventoDeDominio {
}
