package br.com.josewilson.agendamento.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record BloqueioRequest(@NotNull Instant inicio, @NotNull Instant fim, @Size(max = 255) String motivo) {
}
