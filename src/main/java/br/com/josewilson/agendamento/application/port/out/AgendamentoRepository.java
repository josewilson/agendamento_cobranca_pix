package br.com.josewilson.agendamento.application.port.out;

import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;

import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository {

    Agendamento salvar(Agendamento agendamento);

    Optional<Agendamento> buscarPorId(AgendamentoId id);

    List<Agendamento> buscarAtivosPorPrestador(PrestadorId prestadorId);

    List<Agendamento> buscarTodosPendentesPagamento();

    List<Agendamento> buscarPorPrestador(PrestadorId prestadorId);
}
