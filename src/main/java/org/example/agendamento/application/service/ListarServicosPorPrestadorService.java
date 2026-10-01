package org.example.agendamento.application.service;

import org.example.agendamento.application.port.in.ListarServicosPorPrestadorQuery;
import org.example.agendamento.application.port.in.ListarServicosPorPrestadorUseCase;
import org.example.agendamento.application.port.out.ServicoRepository;
import org.example.agendamento.domain.model.servico.Servico;
import org.springframework.stereotype.Service;

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
}
