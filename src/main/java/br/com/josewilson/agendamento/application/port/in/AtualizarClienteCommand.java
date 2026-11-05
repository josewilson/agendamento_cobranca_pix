package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;

/** {@code canalPreferido} opcional: null mantem o valor atual. */
public record AtualizarClienteCommand(ClienteId clienteId, String nome, String email, String telefone,
                                       String canalPreferido) {

    public AtualizarClienteCommand(ClienteId clienteId, String nome, String email, String telefone) {
        this(clienteId, nome, email, telefone, null);
    }
}
