package org.example.agendamento.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CriarAgendamentoRequest(
        @NotNull UUID prestadorId,
        @NotNull UUID clienteId,
        @NotNull UUID servicoId,
        @NotNull Instant inicio,
        @NotNull Instant fim) {
}
