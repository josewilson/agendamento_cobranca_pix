package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.cliente.Cliente;

public interface CadastrarClienteUseCase {
    Cliente executar(CadastrarClienteCommand command);
}
