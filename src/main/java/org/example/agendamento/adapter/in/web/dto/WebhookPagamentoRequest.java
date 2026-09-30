package org.example.agendamento.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record WebhookPagamentoRequest(@NotNull UUID agendamentoId, boolean pago) {
}
