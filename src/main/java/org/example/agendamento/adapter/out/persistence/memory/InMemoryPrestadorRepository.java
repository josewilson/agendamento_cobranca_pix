package org.example.agendamento.adapter.out.persistence.memory;

import org.example.agendamento.application.port.out.PrestadorRepository;
import org.example.agendamento.domain.model.prestador.Prestador;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("dev")
public class InMemoryPrestadorRepository implements PrestadorRepository {

    private final Map<PrestadorId, Prestador> prestadores = new ConcurrentHashMap<>();

    @Override
    public Prestador salvar(Prestador prestador) {
        prestadores.put(prestador.id(), prestador);
        return prestador;
    }

    @Override
    public Optional<Prestador> buscarPorId(PrestadorId id) {
        return Optional.ofNullable(prestadores.get(id));
    }

    @Override
    public Optional<Prestador> buscarPorEmail(String email) {
        return prestadores.values().stream()
                .filter(prestador -> prestador.email().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public List<Prestador> buscarTodos() {
        return new ArrayList<>(prestadores.values());
    }
}
