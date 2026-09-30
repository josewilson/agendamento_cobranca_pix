package org.example.agendamento.adapter.in.web.dto;

import org.example.agendamento.domain.model.agendamento.Agendamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AgendamentoResponse(
        UUID id,
        UUID prestadorId,
        UUID clienteId,
        UUID servicoId,
        Instant inicio,
        Instant fim,
        BigDecimal valorServico,
        BigDecimal valorSinal,
        String status,
        Instant criadoEm) {

    public static AgendamentoResponse de(Agendamento agendamento) {
        return new AgendamentoResponse(
                agendamento.id().valor(),
                agendamento.prestadorId().valor(),
                agendamento.clienteId().valor(),
                agendamento.servicoId().valor(),
                agendamento.periodo().inicio(),
                agendamento.periodo().fim(),
                agendamento.valorServico().valor(),
                agendamento.valorSinal().valor(),
                agendamento.status().name(),
                agendamento.criadoEm());
    }
}
