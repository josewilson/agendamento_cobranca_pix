package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.cliente.Cliente;

import br.com.josewilson.agendamento.application.port.out.Pagina;
import java.util.List;

public interface ListarClientesUseCase {
    List<Cliente> executar();
    Pagina<Cliente> executar(int page, int size);
}
