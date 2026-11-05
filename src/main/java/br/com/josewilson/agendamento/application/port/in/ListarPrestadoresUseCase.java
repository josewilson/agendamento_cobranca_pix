package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.prestador.Prestador;

import java.util.List;

public interface ListarPrestadoresUseCase {
    List<Prestador> executar();
}
