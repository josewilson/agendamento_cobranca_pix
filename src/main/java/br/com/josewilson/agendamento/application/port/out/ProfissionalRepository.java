package br.com.josewilson.agendamento.application.port.out;

import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.Profissional;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;

import java.util.List;
import java.util.Optional;

public interface ProfissionalRepository {

    Profissional salvar(Profissional profissional);

    Optional<Profissional> buscarPorId(ProfissionalId id);

    List<Profissional> buscarPorEstabelecimento(PrestadorId estabelecimentoId);

    void excluir(ProfissionalId id);
}
