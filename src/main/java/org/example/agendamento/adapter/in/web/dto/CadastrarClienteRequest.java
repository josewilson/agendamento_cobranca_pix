package org.example.agendamento.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CadastrarClienteRequest(
        @NotBlank String nome,
        @NotBlank String email,
        @NotBlank String telefone,
        @NotBlank String documentoNumero,
        @NotBlank String documentoTipo) {
}
