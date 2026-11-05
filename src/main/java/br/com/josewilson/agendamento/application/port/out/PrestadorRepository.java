package br.com.josewilson.agendamento.application.port.out;

import br.com.josewilson.agendamento.domain.model.prestador.Prestador;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;

import java.util.List;
import java.util.Optional;

public interface PrestadorRepository {

    Prestador salvar(Prestador prestador);

    Optional<Prestador> buscarPorId(PrestadorId id);

    Optional<Prestador> buscarPorEmail(String email);

    List<Prestador> buscarTodos();
}
