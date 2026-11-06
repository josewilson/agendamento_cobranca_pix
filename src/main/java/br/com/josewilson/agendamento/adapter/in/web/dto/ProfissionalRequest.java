package br.com.josewilson.agendamento.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfissionalRequest(@NotBlank String nome) {
}
