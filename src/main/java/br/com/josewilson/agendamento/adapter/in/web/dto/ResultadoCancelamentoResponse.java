package br.com.josewilson.agendamento.adapter.in.web.dto;

import br.com.josewilson.agendamento.domain.model.agendamento.ResultadoCancelamento;

import java.math.BigDecimal;

public record ResultadoCancelamentoResponse(BigDecimal valorRetido, BigDecimal valorReembolsado) {

    public static ResultadoCancelamentoResponse de(ResultadoCancelamento resultado) {
        return new ResultadoCancelamentoResponse(resultado.valorRetido().valor(), resultado.valorReembolsado().valor());
    }
}
