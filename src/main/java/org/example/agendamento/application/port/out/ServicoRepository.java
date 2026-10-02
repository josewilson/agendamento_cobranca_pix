package org.example.agendamento.application.port.out;

import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.Servico;
import org.example.agendamento.domain.model.servico.ServicoId;

import java.util.List;
import java.util.Optional;

public interface ServicoRepository {

    Servico salvar(Servico servico);

    Optional<Servico> buscarPorId(ServicoId id);

    List<Servico> buscarPorPrestador(PrestadorId prestadorId);

    void excluir(ServicoId id);
}
