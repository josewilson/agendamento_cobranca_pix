package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;

import br.com.josewilson.agendamento.application.port.out.Pagina;
import java.util.List;

public interface ListarAgendamentosPorPrestadorUseCase {
    List<Agendamento> executar(ListarAgendamentosPorPrestadorQuery query);
    Pagina<Agendamento> executar(ListarAgendamentosPorPrestadorQuery query, int page, int size);
}
