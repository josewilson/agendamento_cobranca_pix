package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.port.in.ListarAgendamentosPorPrestadorQuery;
import br.com.josewilson.agendamento.application.port.in.ListarAgendamentosPorPrestadorUseCase;
import br.com.josewilson.agendamento.application.port.out.AgendamentoRepository;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
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
