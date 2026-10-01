package org.example.agendamento.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CadastrarServicoRequest(
        @NotBlank String nome,
        @Positive long duracaoMinutos,
        @NotNull BigDecimal preco,
        @NotNull BigDecimal percentualSinal) {
}
