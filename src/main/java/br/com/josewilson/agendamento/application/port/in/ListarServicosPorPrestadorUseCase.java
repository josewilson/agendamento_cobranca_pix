package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.servico.Servico;

import java.util.List;

public interface ListarServicosPorPrestadorUseCase {
    List<Servico> executar(ListarServicosPorPrestadorQuery query);
}
