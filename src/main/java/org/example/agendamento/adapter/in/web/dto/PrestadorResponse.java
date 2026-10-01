package org.example.agendamento.adapter.in.web.dto;

import org.example.agendamento.domain.model.prestador.Prestador;

import java.util.UUID;

public record PrestadorResponse(UUID id, String nome, String documentoNumero, String documentoTipo) {

    public static PrestadorResponse de(Prestador prestador) {
        return new PrestadorResponse(
                prestador.id().valor(),
                prestador.nome(),
                prestador.documento().numero(),
                prestador.documento().tipo().name());
    }
}
