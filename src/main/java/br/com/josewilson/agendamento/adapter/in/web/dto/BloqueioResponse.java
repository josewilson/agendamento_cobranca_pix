package br.com.josewilson.agendamento.adapter.in.web.dto;

import br.com.josewilson.agendamento.domain.model.profissional.BloqueioDeAgenda;

import java.time.Instant;
import java.util.UUID;

public record BloqueioResponse(UUID id, Instant inicio, Instant fim, String motivo) {

    public static BloqueioResponse de(BloqueioDeAgenda bloqueio) {
        return new BloqueioResponse(bloqueio.id().valor(), bloqueio.periodo().inicio(), bloqueio.periodo().fim(),
                bloqueio.motivo());
    }
}
