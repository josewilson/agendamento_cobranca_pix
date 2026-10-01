package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.prestador.Prestador;

public interface CadastrarPrestadorUseCase {
    Prestador executar(CadastrarPrestadorCommand command);
}
