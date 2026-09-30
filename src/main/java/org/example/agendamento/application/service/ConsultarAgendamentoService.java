package org.example.agendamento.application.service;

import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.application.port.in.ConsultarAgendamentoQuery;
import org.example.agendamento.application.port.in.ConsultarAgendamentoUseCase;
import org.example.agendamento.application.port.out.AgendamentoRepository;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.springframework.stereotype.Service;

@Service
public class ConsultarAgendamentoService implements ConsultarAgendamentoUseCase {

    private final AgendamentoRepository agendamentoRepository;

    public ConsultarAgendamentoService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    @Override
    public Agendamento executar(ConsultarAgendamentoQuery query) {
        return agendamentoRepository.buscarPorId(query.agendamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento nao encontrado: " + query.agendamentoId()));
    }
}
