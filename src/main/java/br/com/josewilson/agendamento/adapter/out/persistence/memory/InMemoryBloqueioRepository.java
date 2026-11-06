package br.com.josewilson.agendamento.adapter.out.persistence.memory;

import br.com.josewilson.agendamento.application.port.out.BloqueioRepository;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioDeAgenda;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioId;
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
public class InMemoryBloqueioRepository implements BloqueioRepository {

    private final Map<BloqueioId, BloqueioDeAgenda> bloqueios = new ConcurrentHashMap<>();

    @Override
    public BloqueioDeAgenda salvar(BloqueioDeAgenda bloqueio) {
        bloqueios.put(bloqueio.id(), bloqueio);
        return bloqueio;
    }

    @Override
    public Optional<BloqueioDeAgenda> buscarPorId(BloqueioId id) {
        return Optional.ofNullable(bloqueios.get(id));
    }

    @Override
    public List<BloqueioDeAgenda> buscarPorProfissional(ProfissionalId profissionalId) {
        return bloqueios.values().stream()
                .filter(b -> b.profissionalId().equals(profissionalId))
                .sorted(Comparator.comparing(b -> b.periodo().inicio()))
                .toList();
    }

    @Override
    public void excluir(BloqueioId id) {
        bloqueios.remove(id);
    }
}
