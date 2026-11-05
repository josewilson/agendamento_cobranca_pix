package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.prestador.Prestador;

public interface AtualizarPrestadorUseCase {
    Prestador executar(AtualizarPrestadorCommand command);
}
