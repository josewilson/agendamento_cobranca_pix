package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.exception.RecursoNaoEncontradoException;
import br.com.josewilson.agendamento.application.port.in.ConsultarAgendamentoQuery;
import br.com.josewilson.agendamento.application.port.in.ConsultarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.out.AgendamentoRepository;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
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
