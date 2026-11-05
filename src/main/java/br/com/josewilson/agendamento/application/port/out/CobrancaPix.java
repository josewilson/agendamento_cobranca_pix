package br.com.josewilson.agendamento.application.port.out;

import java.time.Instant;

public record CobrancaPix(String referenciaExterna, String qrCode, String copiaECola, Instant expiraEm) {
}
