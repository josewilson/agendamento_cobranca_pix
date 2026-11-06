package br.com.josewilson.agendamento.application.port.out;

import br.com.josewilson.agendamento.domain.model.profissional.BloqueioDeAgenda;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioId;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;

import java.util.List;
import java.util.Optional;

public interface BloqueioRepository {

    BloqueioDeAgenda salvar(BloqueioDeAgenda bloqueio);

    Optional<BloqueioDeAgenda> buscarPorId(BloqueioId id);

    /** Ordenados por inicio. */
    List<BloqueioDeAgenda> buscarPorProfissional(ProfissionalId profissionalId);

    void excluir(BloqueioId id);
}
