package br.com.josewilson.agendamento.adapter.in.web.dto;

import br.com.josewilson.agendamento.domain.model.cliente.Cliente;

import java.util.UUID;

public record ClienteResponse(UUID id, String nome, String email, String telefone,
                               String documentoNumero, String documentoTipo, int quantidadeNoShow,
                               String canalPreferido) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
                cliente.id().valor(),
                cliente.nome(),
                cliente.contato().email(),
                cliente.contato().telefone(),
                cliente.documento().numero(),
                cliente.documento().tipo().name(),
                cliente.quantidadeNoShow(),
                cliente.canalPreferido().name());
    }
}
