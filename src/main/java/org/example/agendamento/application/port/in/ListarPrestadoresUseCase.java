package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.prestador.Prestador;

import java.util.List;

public interface ListarPrestadoresUseCase {
    List<Prestador> executar();
}
