package br.com.josewilson.agendamento.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/** {@code profissionalId} opcional se o prestador tem um unico profissional; o cliente vem da sessao. */
public record CriarAgendamentoRequest(
        @NotNull UUID prestadorId,
        @NotNull UUID servicoId,
        UUID profissionalId,
        @NotNull Instant inicio,
        @NotNull Instant fim) {
}
