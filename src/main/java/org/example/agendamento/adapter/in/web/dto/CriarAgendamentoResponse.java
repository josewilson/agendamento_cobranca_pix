package org.example.agendamento.adapter.in.web.dto;

import org.example.agendamento.application.port.in.ResultadoCriacaoAgendamento;

public record CriarAgendamentoResponse(AgendamentoResponse agendamento, CobrancaPixResponse cobranca) {

    public static CriarAgendamentoResponse de(ResultadoCriacaoAgendamento resultado) {
        return new CriarAgendamentoResponse(
                AgendamentoResponse.de(resultado.agendamento()),
                resultado.cobranca().map(CobrancaPixResponse::de).orElse(null));
    }
}
