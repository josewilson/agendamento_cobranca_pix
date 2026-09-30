package org.example.agendamento.application.port.out;

import java.time.Instant;

public record CobrancaPix(String referenciaExterna, String qrCode, String copiaECola, Instant expiraEm) {
}
