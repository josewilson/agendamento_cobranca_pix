package org.example.agendamento.adapter.out.persistence.memory;

import org.example.agendamento.application.port.out.ServicoRepository;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.Servico;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("dev")
public class InMemoryServicoRepository implements ServicoRepository {

    private final Map<ServicoId, Servico> servicos = new ConcurrentHashMap<>();

    @Override
    public Servico salvar(Servico servico) {
        servicos.put(servico.id(), servico);
        return servico;
    }

    @Override
    public Optional<Servico> buscarPorId(ServicoId id) {
        return Optional.ofNullable(servicos.get(id));
    }

    @Override
    public List<Servico> buscarPorPrestador(PrestadorId prestadorId) {
        return servicos.values().stream()
                .filter(servico -> servico.prestadorId().equals(prestadorId))
                .toList();
    }

    @Override
    public void excluir(ServicoId id) {
        servicos.remove(id);
    }
}
