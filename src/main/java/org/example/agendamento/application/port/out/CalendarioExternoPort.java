package org.example.agendamento.application.port.out;

import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.shared.Periodo;

public interface CalendarioExternoPort {

    void sincronizarEvento(AgendamentoId agendamentoId, String titulo, String descricao, Periodo periodo);

    void removerEvento(AgendamentoId agendamentoId);
}
