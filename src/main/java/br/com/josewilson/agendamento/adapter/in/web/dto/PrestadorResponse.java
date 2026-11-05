package br.com.josewilson.agendamento.adapter.in.web.dto;

import br.com.josewilson.agendamento.domain.model.prestador.Prestador;

import java.util.UUID;

public record PrestadorResponse(UUID id, String nome, String telefone, String email, String documentoNumero, String documentoTipo) {

    public static PrestadorResponse de(Prestador prestador) {
        return new PrestadorResponse(
                prestador.id().valor(),
                prestador.nome(),
                prestador.telefone(),
                prestador.email(),
                prestador.documento().numero(),
                prestador.documento().tipo().name());
    }
}
