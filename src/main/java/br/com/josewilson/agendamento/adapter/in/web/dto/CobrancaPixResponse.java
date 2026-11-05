package br.com.josewilson.agendamento.adapter.in.web.dto;

import br.com.josewilson.agendamento.application.port.out.CobrancaPix;

import java.time.Instant;

public record CobrancaPixResponse(String referenciaExterna, String qrCode, String copiaECola, Instant expiraEm) {

    public static CobrancaPixResponse de(CobrancaPix cobranca) {
        return new CobrancaPixResponse(cobranca.referenciaExterna(), cobranca.qrCode(), cobranca.copiaECola(),
                cobranca.expiraEm());
    }
}
