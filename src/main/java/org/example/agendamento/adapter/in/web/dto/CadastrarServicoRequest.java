package org.example.agendamento.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CadastrarServicoRequest(
        @NotNull UUID prestadorId,
        @NotBlank String nome,
        @Positive long duracaoMinutos,
        @NotNull BigDecimal preco,
        @NotNull BigDecimal percentualSinal) {
}
