package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.servico.Servico;

import java.util.List;

public interface ListarServicosPorPrestadorUseCase {
    List<Servico> executar(ListarServicosPorPrestadorQuery query);
}
