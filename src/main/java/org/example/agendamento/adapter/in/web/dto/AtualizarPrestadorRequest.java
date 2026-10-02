package org.example.agendamento.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarPrestadorRequest(
        @NotBlank String nome,
        @NotBlank String telefone) {
}
