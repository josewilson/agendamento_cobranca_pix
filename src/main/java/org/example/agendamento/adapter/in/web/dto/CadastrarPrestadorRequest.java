package org.example.agendamento.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CadastrarPrestadorRequest(
        @NotBlank String nome,
        @NotBlank String telefone,
        @NotBlank String email,
        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
                message = "deve ter no minimo 8 caracteres, com pelo menos uma letra e um numero")
        String senha,
        @NotBlank String documentoNumero,
        @NotBlank String documentoTipo) {
}
