package br.com.josewilson.agendamento.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AtualizarClienteRequest(
        @NotBlank String nome,
        @NotBlank String email,
        @NotBlank String telefone,
        @Pattern(regexp = "(?i)AMBOS|EMAIL|WHATSAPP") String canalPreferido) {
}
