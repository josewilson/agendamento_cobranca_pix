package br.com.josewilson.agendamento.adapter.out.persistence.memory;

import br.com.josewilson.agendamento.application.port.out.AgendamentoRepository;
import br.com.josewilson.agendamento.application.port.out.Pagina;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.agendamento.StatusAgendamento;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
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

    @Override
    public List<Agendamento> buscarPorPrestador(PrestadorId prestadorId) {
        return agendamentos.values().stream()
                .filter(agendamento -> agendamento.prestadorId().equals(prestadorId))
                .sorted(Comparator.comparing(agendamento -> agendamento.periodo().inicio()))
                .toList();
    }

    @Override
    public Pagina<Agendamento> buscarPaginaPorPrestador(PrestadorId prestadorId, int page, int size) {
        return Pagina.de(buscarPorPrestador(prestadorId), page, size);
    }
}
