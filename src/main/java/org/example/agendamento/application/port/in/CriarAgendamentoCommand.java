package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.example.agendamento.domain.model.shared.Periodo;

public record CriarAgendamentoCommand(PrestadorId prestadorId, ClienteId clienteId, ServicoId servicoId,
                                       Periodo periodo) {
}
