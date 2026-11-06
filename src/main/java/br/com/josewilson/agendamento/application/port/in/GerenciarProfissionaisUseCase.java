package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.Profissional;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;

import java.util.List;

/** Profissionais de um estabelecimento (Prestador). Quem altera e sempre o proprio estabelecimento. */
public interface GerenciarProfissionaisUseCase {

    Profissional cadastrar(PrestadorId estabelecimentoId, String nome);

    List<Profissional> listar(PrestadorId estabelecimentoId);

    Profissional atualizar(PrestadorId solicitante, ProfissionalId id, String nome);

    /** Recusa excluir o ultimo profissional do estabelecimento (sem profissional ninguem agenda). */
    void excluir(PrestadorId solicitante, ProfissionalId id);
}
