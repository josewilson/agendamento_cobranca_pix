package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.servico.Servico;

import br.com.josewilson.agendamento.application.port.out.Pagina;
import java.util.List;

public interface ListarServicosPorPrestadorUseCase {
    List<Servico> executar(ListarServicosPorPrestadorQuery query);
    Pagina<Servico> executar(ListarServicosPorPrestadorQuery query, int page, int size);
}
