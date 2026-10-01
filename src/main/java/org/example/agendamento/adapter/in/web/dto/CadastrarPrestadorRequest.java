package org.example.agendamento.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CadastrarPrestadorRequest(
        @NotBlank String nome,
        @NotBlank String telefone,
        @NotBlank String documentoNumero,
        @NotBlank String documentoTipo) {
}
