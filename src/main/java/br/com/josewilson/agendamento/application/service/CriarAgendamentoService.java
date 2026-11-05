package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.application.exception.RecursoNaoEncontradoException;
import br.com.josewilson.agendamento.application.port.in.CriarAgendamentoCommand;
import br.com.josewilson.agendamento.application.port.in.CriarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.in.ResultadoCriacaoAgendamento;
import br.com.josewilson.agendamento.application.port.out.AgendamentoRepository;
import br.com.josewilson.agendamento.application.port.out.Clock;
import br.com.josewilson.agendamento.application.port.out.ClienteRepository;
import br.com.josewilson.agendamento.application.port.out.CobrancaPix;
import br.com.josewilson.agendamento.application.port.out.GatewayDePagamento;
import br.com.josewilson.agendamento.application.port.out.PrestadorRepository;
import br.com.josewilson.agendamento.application.port.out.PublicadorDeEventos;
import br.com.josewilson.agendamento.application.port.out.ServicoRepository;
import br.com.josewilson.agendamento.domain.event.AgendamentoCriado;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.prestador.Prestador;
import br.com.josewilson.agendamento.domain.model.servico.Servico;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
import br.com.josewilson.agendamento.domain.service.VerificadorDeConflito;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CriarAgendamentoService implements CriarAgendamentoUseCase {

    private static final BigDecimal PERCENTUAL_SINAL_MINIMO_PARA_CLIENTE_COM_NO_SHOW = BigDecimal.valueOf(50);

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final PrestadorRepository prestadorRepository;
    private final ServicoRepository servicoRepository;
    private final GatewayDePagamento gatewayDePagamento;
    private final PublicadorDeEventos publicadorDeEventos;
    private final Clock clock;

    public CriarAgendamentoService(AgendamentoRepository agendamentoRepository, ClienteRepository clienteRepository,
                                    PrestadorRepository prestadorRepository, ServicoRepository servicoRepository,
                                    GatewayDePagamento gatewayDePagamento, PublicadorDeEventos publicadorDeEventos,
                                    Clock clock) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.prestadorRepository = prestadorRepository;
        this.servicoRepository = servicoRepository;
        this.gatewayDePagamento = gatewayDePagamento;
        this.publicadorDeEventos = publicadorDeEventos;
        this.clock = clock;
    }

    @Override
    public ResultadoCriacaoAgendamento executar(CriarAgendamentoCommand command) {
        Prestador prestador = prestadorRepository.buscarPorId(command.prestadorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prestador nao encontrado: " + command.prestadorId()));
        Cliente cliente = clienteRepository.buscarPorId(command.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado: " + command.clienteId()));
        Servico servico = servicoRepository.buscarPorId(command.servicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Servico nao encontrado: " + command.servicoId()));

        List<Agendamento> agendamentosAtivos = agendamentoRepository.buscarAtivosPorPrestador(command.prestadorId());
        VerificadorDeConflito.verificarDisponibilidade(command.periodo(), agendamentosAtivos);

        Dinheiro valorSinal = calcularValorSinal(servico, cliente);
        boolean exigeSinal = valorSinal.maiorQue(Dinheiro.ZERO);

        Instant agora = clock.agora();
        Agendamento agendamento = Agendamento.criar(AgendamentoId.novo(), prestador.id(), cliente.id(), servico.id(),
                command.periodo(), servico.preco(), valorSinal, prestador.politicaCancelamentoPadrao(), agora);

        // Persiste (e faz flush) ANTES de cobrar: a constraint de exclusao do banco recusa o horario
        // concorrente aqui, sem ter gerado cobranca no gateway. Se o gateway falhar depois, a
        // transacao inteira da rollback — nem agendamento nem cobranca orfaos.
        agendamentoRepository.salvar(agendamento);

        Optional<CobrancaPix> cobranca = Optional.empty();
        if (exigeSinal) {
            CobrancaPix cobrancaPix = gatewayDePagamento.gerarCobrancaPix(agendamento.id(), cliente, valorSinal);
            agendamento.vincularReferenciaPagamento(cobrancaPix.referenciaExterna());
            agendamentoRepository.salvar(agendamento);
            cobranca = Optional.of(cobrancaPix);
        }

        publicadorDeEventos.publicar(new AgendamentoCriado(agendamento.id(), prestador.id(), cliente.id(),
                agendamento.periodo(), agora));

        return new ResultadoCriacaoAgendamento(agendamento, cobranca);
    }

    private Dinheiro calcularValorSinal(Servico servico, Cliente cliente) {
        if (servico.exigeSinal()) {
            return servico.calcularSinal();
        }
        if (cliente.exigeSinalObrigatorio()) {
            return servico.preco().percentual(PERCENTUAL_SINAL_MINIMO_PARA_CLIENTE_COM_NO_SHOW);
        }
        return Dinheiro.ZERO;
    }
}
