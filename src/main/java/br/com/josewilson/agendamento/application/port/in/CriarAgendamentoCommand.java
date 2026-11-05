package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.servico.ServicoId;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;

public record CriarAgendamentoCommand(PrestadorId prestadorId, ClienteId clienteId, ServicoId servicoId,
                                       Periodo periodo) {
}
