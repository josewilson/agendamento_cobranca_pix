package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.exception.RecursoNaoEncontradoException;
import br.com.josewilson.agendamento.application.port.in.CancelarAgendamentoCommand;
import br.com.josewilson.agendamento.application.port.in.CancelarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.out.AgendamentoRepository;
import br.com.josewilson.agendamento.application.port.out.Clock;
import br.com.josewilson.agendamento.application.port.out.PublicadorDeEventos;
import br.com.josewilson.agendamento.domain.event.AgendamentoCancelado;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import br.com.josewilson.agendamento.domain.model.agendamento.ResultadoCancelamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
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
