package br.com.josewilson.agendamento.domain.event;

import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.agendamento.ResultadoCancelamento;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;

import java.time.Instant;

public record AgendamentoCancelado(AgendamentoId agendamentoId, ClienteId clienteId, ResultadoCancelamento resultado,
                                    Instant ocorridoEm) implements EventoDeDominio {
}
