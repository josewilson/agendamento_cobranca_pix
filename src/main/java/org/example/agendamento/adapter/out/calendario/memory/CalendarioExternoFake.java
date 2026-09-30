package org.example.agendamento.adapter.out.calendario.memory;

import org.example.agendamento.application.port.out.CalendarioExternoPort;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.shared.Periodo;
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
