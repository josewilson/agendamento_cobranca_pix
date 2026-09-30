package org.example.agendamento.adapter.out.persistence.memory;

import org.example.agendamento.application.port.out.AgendamentoRepository;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.agendamento.StatusAgendamento;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("dev")
public class InMemoryAgendamentoRepository implements AgendamentoRepository {

    private static final Set<StatusAgendamento> STATUS_ATIVOS =
            Set.of(StatusAgendamento.PENDENTE_PAGAMENTO, StatusAgendamento.CONFIRMADO);

    private final Map<AgendamentoId, Agendamento> agendamentos = new ConcurrentHashMap<>();

    @Override
    public Agendamento salvar(Agendamento agendamento) {
        agendamentos.put(agendamento.id(), agendamento);
        return agendamento;
    }

    @Override
    public Optional<Agendamento> buscarPorId(AgendamentoId id) {
        return Optional.ofNullable(agendamentos.get(id));
    }

    @Override
    public List<Agendamento> buscarAtivosPorPrestador(PrestadorId prestadorId) {
        return agendamentos.values().stream()
                .filter(agendamento -> agendamento.prestadorId().equals(prestadorId))
                .filter(agendamento -> STATUS_ATIVOS.contains(agendamento.status()))
                .toList();
    }

    @Override
    public List<Agendamento> buscarTodosPendentesPagamento() {
        return agendamentos.values().stream()
                .filter(agendamento -> agendamento.status() == StatusAgendamento.PENDENTE_PAGAMENTO)
                .toList();
    }
}
