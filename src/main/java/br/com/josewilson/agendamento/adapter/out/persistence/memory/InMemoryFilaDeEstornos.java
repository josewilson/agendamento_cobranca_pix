package br.com.josewilson.agendamento.adapter.out.persistence.memory;

import br.com.josewilson.agendamento.application.port.out.FilaDeEstornos;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("dev")
public class InMemoryFilaDeEstornos implements FilaDeEstornos {

    private final ConcurrentHashMap<UUID, EstornoPendente> fila = new ConcurrentHashMap<>();

    @Override
    public void registrar(AgendamentoId agendamentoId, String referencia, Dinheiro valor, String erro, Instant agora) {
        UUID id = UUID.randomUUID();
        fila.put(id, new EstornoPendente(id, agendamentoId, referencia, valor, 1));
    }

    @Override
    public List<EstornoPendente> buscarPendentes(int maxTentativas) {
        return fila.values().stream().filter(e -> e.tentativas() < maxTentativas).toList();
    }

    @Override
    public void registrarNovaFalha(UUID id, String erro) {
        fila.computeIfPresent(id, (k, e) ->
                new EstornoPendente(e.id(), e.agendamentoId(), e.referencia(), e.valor(), e.tentativas() + 1));
    }

    @Override
    public void concluir(UUID id) {
        fila.remove(id);
    }
}
