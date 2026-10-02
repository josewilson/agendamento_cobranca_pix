package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.ServicoId;

public record ExcluirServicoCommand(ServicoId servicoId, PrestadorId prestadorAutenticado) {
}
