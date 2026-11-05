package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;

import java.util.List;

public interface ListarAgendamentosPorPrestadorUseCase {
    List<Agendamento> executar(ListarAgendamentosPorPrestadorQuery query);
}
