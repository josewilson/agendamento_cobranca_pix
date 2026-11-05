package br.com.josewilson.agendamento.domain.event;

import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;

import java.time.Instant;

public record AgendamentoCriado(AgendamentoId agendamentoId, PrestadorId prestadorId, ClienteId clienteId,
                                 Periodo periodo, Instant ocorridoEm) implements EventoDeDominio {
}
