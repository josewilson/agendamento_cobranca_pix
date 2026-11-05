package br.com.josewilson.agendamento.adapter.out.calendario.memory;

import br.com.josewilson.agendamento.application.port.out.CalendarioExternoPort;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("dev")
public class CalendarioExternoFake implements CalendarioExternoPort {

    private final Map<AgendamentoId, String> eventosSincronizados = new ConcurrentHashMap<>();

    @Override
    public void sincronizarEvento(AgendamentoId agendamentoId, String titulo, String descricao, Periodo periodo) {
        eventosSincronizados.put(agendamentoId, titulo);
    }

    @Override
    public void removerEvento(AgendamentoId agendamentoId) {
        eventosSincronizados.remove(agendamentoId);
    }

    public Map<AgendamentoId, String> eventosSincronizados() {
        return eventosSincronizados;
    }
}
