package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.port.in.ListarServicosPorPrestadorQuery;
import br.com.josewilson.agendamento.application.port.in.ListarServicosPorPrestadorUseCase;
import br.com.josewilson.agendamento.application.port.out.ServicoRepository;
import br.com.josewilson.agendamento.domain.model.servico.Servico;
import org.springframework.stereotype.Service;

import br.com.josewilson.agendamento.application.port.out.Pagina;
import java.util.List;

@Service
public class ListarServicosPorPrestadorService implements ListarServicosPorPrestadorUseCase {

    private final ServicoRepository servicoRepository;

    public ListarServicosPorPrestadorService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Override
    public List<Servico> executar(ListarServicosPorPrestadorQuery query) {
        return servicoRepository.buscarPorPrestador(query.prestadorId());
    }

    @Override
    public Pagina<Servico> executar(ListarServicosPorPrestadorQuery query, int page, int size) {
        return servicoRepository.buscarPaginaPorPrestador(query.prestadorId(), page, size);
    }
}
