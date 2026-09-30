package org.example.agendamento.application.service;

import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.application.port.in.ConfirmarAgendamentoCommand;
import org.example.agendamento.application.port.in.ConfirmarAgendamentoUseCase;
import org.example.agendamento.application.port.out.AgendamentoRepository;
import org.example.agendamento.application.port.out.Clock;
import org.example.agendamento.application.port.out.PublicadorDeEventos;
import org.example.agendamento.domain.event.AgendamentoConfirmado;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.springframework.stereotype.Service;

@Service
public class ConfirmarAgendamentoService implements ConfirmarAgendamentoUseCase {

    private final AgendamentoRepository agendamentoRepository;
    private final PublicadorDeEventos publicadorDeEventos;
    private final Clock clock;

    public ConfirmarAgendamentoService(AgendamentoRepository agendamentoRepository,
                                        PublicadorDeEventos publicadorDeEventos, Clock clock) {
        this.agendamentoRepository = agendamentoRepository;
        this.publicadorDeEventos = publicadorDeEventos;
        this.clock = clock;
    }

    @Override
    public Agendamento executar(ConfirmarAgendamentoCommand command) {
        Agendamento agendamento = agendamentoRepository.buscarPorId(command.agendamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento nao encontrado: " + command.agendamentoId()));

        agendamento.confirmar();
        agendamentoRepository.salvar(agendamento);
        publicadorDeEventos.publicar(new AgendamentoConfirmado(agendamento.id(), agendamento.clienteId(), clock.agora()));

        return agendamento;
    }
}
