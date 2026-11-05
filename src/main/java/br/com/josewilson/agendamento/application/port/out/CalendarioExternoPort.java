package br.com.josewilson.agendamento.application.port.out;

import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;

public interface CalendarioExternoPort {

    void sincronizarEvento(AgendamentoId agendamentoId, String titulo, String descricao, Periodo periodo);

    void removerEvento(AgendamentoId agendamentoId);
}
