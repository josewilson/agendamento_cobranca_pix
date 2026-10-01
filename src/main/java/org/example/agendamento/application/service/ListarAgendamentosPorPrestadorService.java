package org.example.agendamento.application.service;

import org.example.agendamento.application.port.in.ListarAgendamentosPorPrestadorQuery;
import org.example.agendamento.application.port.in.ListarAgendamentosPorPrestadorUseCase;
import org.example.agendamento.application.port.out.AgendamentoRepository;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarAgendamentosPorPrestadorService implements ListarAgendamentosPorPrestadorUseCase {

    private final AgendamentoRepository agendamentoRepository;

    public ListarAgendamentosPorPrestadorService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    @Override
    public List<Agendamento> executar(ListarAgendamentosPorPrestadorQuery query) {
        return agendamentoRepository.buscarPorPrestador(query.prestadorId());
    }
}
