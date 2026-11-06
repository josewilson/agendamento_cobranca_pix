package br.com.josewilson.agendamento.adapter.in.web.dto;

import br.com.josewilson.agendamento.domain.model.profissional.Profissional;

import java.util.UUID;

public record ProfissionalResponse(UUID id, String nome) {

    public static ProfissionalResponse de(Profissional profissional) {
        return new ProfissionalResponse(profissional.id().valor(), profissional.nome());
    }
}
