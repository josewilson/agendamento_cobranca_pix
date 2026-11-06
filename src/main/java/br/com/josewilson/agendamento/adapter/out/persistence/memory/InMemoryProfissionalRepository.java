package br.com.josewilson.agendamento.adapter.out.persistence.memory;

import br.com.josewilson.agendamento.application.port.out.ProfissionalRepository;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.Profissional;
import br.com.josewilson.agendamento.domain.model.profissional.ProfissionalId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("dev")
public class InMemoryProfissionalRepository implements ProfissionalRepository {

    private final Map<ProfissionalId, Profissional> profissionais = new ConcurrentHashMap<>();

    @Override
    public Profissional salvar(Profissional profissional) {
        profissionais.put(profissional.id(), profissional);
        return profissional;
    }

    @Override
    public Optional<Profissional> buscarPorId(ProfissionalId id) {
        return Optional.ofNullable(profissionais.get(id));
    }

    @Override
    public List<Profissional> buscarPorEstabelecimento(PrestadorId estabelecimentoId) {
        return profissionais.values().stream()
                .filter(p -> p.estabelecimentoId().equals(estabelecimentoId))
                .sorted(Comparator.comparing(Profissional::nome))
                .toList();
    }

    @Override
    public void excluir(ProfissionalId id) {
        profissionais.remove(id);
    }
}
