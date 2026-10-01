package org.example.agendamento.application.port.out;

import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.prestador.PrestadorId;

import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository {

    Agendamento salvar(Agendamento agendamento);

    Optional<Agendamento> buscarPorId(AgendamentoId id);

    List<Agendamento> buscarAtivosPorPrestador(PrestadorId prestadorId);

    List<Agendamento> buscarTodosPendentesPagamento();

    List<Agendamento> buscarPorPrestador(PrestadorId prestadorId);
}
