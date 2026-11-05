package br.com.josewilson.agendamento.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarClienteRequest(
        @NotBlank String nome,
        @NotBlank String email,
        @NotBlank String telefone) {
}
