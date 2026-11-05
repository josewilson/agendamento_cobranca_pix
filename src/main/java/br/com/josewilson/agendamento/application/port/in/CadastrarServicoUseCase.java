package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.servico.Servico;

public interface CadastrarServicoUseCase {
    Servico executar(CadastrarServicoCommand command);
}
