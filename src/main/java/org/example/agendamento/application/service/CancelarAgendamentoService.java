package org.example.agendamento.application.service;

import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.application.port.in.CancelarAgendamentoCommand;
import org.example.agendamento.application.port.in.CancelarAgendamentoUseCase;
import org.example.agendamento.application.port.out.AgendamentoRepository;
import org.example.agendamento.application.port.out.Clock;
import org.example.agendamento.application.port.out.PublicadorDeEventos;
import org.example.agendamento.domain.event.AgendamentoCancelado;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.example.agendamento.domain.model.agendamento.ResultadoCancelamento;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CancelarAgendamentoService implements CancelarAgendamentoUseCase {

    private final AgendamentoRepository agendamentoRepository;
    private final PublicadorDeEventos publicadorDeEventos;
    private final Clock clock;

    public CancelarAgendamentoService(AgendamentoRepository agendamentoRepository,
                                       PublicadorDeEventos publicadorDeEventos, Clock clock) {
        this.agendamentoRepository = agendamentoRepository;
        this.publicadorDeEventos = publicadorDeEventos;
        this.clock = clock;
    }

    @Override
    public ResultadoCancelamento executar(CancelarAgendamentoCommand command) {
        Agendamento agendamento = agendamentoRepository.buscarPorId(command.agendamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento nao encontrado: " + command.agendamentoId()));

        Instant agora = clock.agora();
        ResultadoCancelamento resultado = agendamento.cancelar(agora);
        agendamentoRepository.salvar(agendamento);
        publicadorDeEventos.publicar(new AgendamentoCancelado(agendamento.id(), agendamento.clienteId(), resultado, agora));

        return resultado;
    }
}
