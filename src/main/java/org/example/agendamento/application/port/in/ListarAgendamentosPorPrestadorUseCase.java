package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.agendamento.Agendamento;

import java.util.List;

public interface ListarAgendamentosPorPrestadorUseCase {
    List<Agendamento> executar(ListarAgendamentosPorPrestadorQuery query);
}
