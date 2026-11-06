package br.com.josewilson.agendamento.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CadastrarClienteRequest(
        @NotBlank String nome,
        @NotBlank String email,
        @NotBlank String telefone,
        @NotBlank String documentoNumero,
        @NotBlank String documentoTipo,
        @Pattern(regexp = "(?i)AMBOS|EMAIL|WHATSAPP") String canalPreferido,
        @NotBlank @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$", message = "senha deve ter no minimo 8 caracteres, com letra e numero") String senha) {
}
