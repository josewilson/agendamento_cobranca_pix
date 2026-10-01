package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.servico.Servico;

public interface CadastrarServicoUseCase {
    Servico executar(CadastrarServicoCommand command);
}
