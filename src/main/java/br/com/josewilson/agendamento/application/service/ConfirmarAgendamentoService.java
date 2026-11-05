package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.exception.RecursoNaoEncontradoException;
import br.com.josewilson.agendamento.application.port.in.ConfirmarAgendamentoCommand;
import br.com.josewilson.agendamento.application.port.in.ConfirmarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.out.AgendamentoRepository;
import br.com.josewilson.agendamento.application.port.out.Clock;
import br.com.josewilson.agendamento.application.port.out.PublicadorDeEventos;
import br.com.josewilson.agendamento.domain.event.AgendamentoConfirmado;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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
