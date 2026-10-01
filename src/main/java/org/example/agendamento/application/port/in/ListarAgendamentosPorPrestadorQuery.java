package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.prestador.PrestadorId;

public record ListarAgendamentosPorPrestadorQuery(PrestadorId prestadorId) {
}
