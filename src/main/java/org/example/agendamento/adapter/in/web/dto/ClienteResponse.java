package org.example.agendamento.adapter.in.web.dto;

import org.example.agendamento.domain.model.cliente.Cliente;

import java.util.UUID;

public record ClienteResponse(UUID id, String nome, String email, String telefone,
                               String documentoNumero, String documentoTipo, int quantidadeNoShow) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
                cliente.id().valor(),
                cliente.nome(),
                cliente.contato().email(),
                cliente.contato().telefone(),
                cliente.documento().numero(),
                cliente.documento().tipo().name(),
                cliente.quantidadeNoShow());
    }
}
