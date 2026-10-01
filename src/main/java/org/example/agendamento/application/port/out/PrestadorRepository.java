package org.example.agendamento.application.port.out;

import org.example.agendamento.domain.model.prestador.Prestador;
import org.example.agendamento.domain.model.prestador.PrestadorId;

import java.util.List;
import java.util.Optional;

public interface PrestadorRepository {

    Prestador salvar(Prestador prestador);

    Optional<Prestador> buscarPorId(PrestadorId id);

    List<Prestador> buscarTodos();
}
