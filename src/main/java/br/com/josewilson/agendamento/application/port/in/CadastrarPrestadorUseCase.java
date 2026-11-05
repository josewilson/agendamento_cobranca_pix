package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.prestador.Prestador;

public interface CadastrarPrestadorUseCase {
    Prestador executar(CadastrarPrestadorCommand command);
}
