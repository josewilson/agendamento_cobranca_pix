package org.example.agendamento.adapter.in.web.dto;

import org.example.agendamento.domain.model.agendamento.ResultadoCancelamento;

import java.math.BigDecimal;

public record ResultadoCancelamentoResponse(BigDecimal valorRetido, BigDecimal valorReembolsado) {

    public static ResultadoCancelamentoResponse de(ResultadoCancelamento resultado) {
        return new ResultadoCancelamentoResponse(resultado.valorRetido().valor(), resultado.valorReembolsado().valor());
    }
}
