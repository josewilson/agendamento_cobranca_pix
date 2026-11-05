package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;

public record ListarServicosPorPrestadorQuery(PrestadorId prestadorId) {
}
