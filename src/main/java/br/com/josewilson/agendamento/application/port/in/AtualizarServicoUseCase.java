package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.servico.Servico;

public interface AtualizarServicoUseCase {
    Servico executar(AtualizarServicoCommand command);
}
