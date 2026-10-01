package org.example.agendamento.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastrarPrestadorRequest(
        @NotBlank String nome,
        @NotBlank String telefone,
        @NotBlank String email,
        @NotBlank @Size(min = 6, message = "deve ter no minimo 6 caracteres") String senha,
        @NotBlank String documentoNumero,
        @NotBlank String documentoTipo) {
}
