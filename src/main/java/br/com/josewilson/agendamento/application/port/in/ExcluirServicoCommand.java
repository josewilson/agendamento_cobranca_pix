package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.servico.ServicoId;

public record ExcluirServicoCommand(ServicoId servicoId, PrestadorId prestadorAutenticado) {
}
