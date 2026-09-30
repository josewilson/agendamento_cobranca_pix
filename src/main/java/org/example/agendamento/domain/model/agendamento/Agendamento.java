package org.example.agendamento.domain.model.agendamento;

import org.example.agendamento.domain.exception.TransicaoDeStatusInvalidaException;
import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.example.agendamento.domain.model.shared.Periodo;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class Agendamento {

    private final AgendamentoId id;
    private final PrestadorId prestadorId;
    private final ClienteId clienteId;
    private final ServicoId servicoId;
    private final Periodo periodo;
    private final Dinheiro valorServico;
    private final Dinheiro valorSinal;
    private final PoliticaCancelamento politicaAplicada;
    private final Instant criadoEm;
    private StatusAgendamento status;
    private String referenciaPagamento;

    private Agendamento(AgendamentoId id, PrestadorId prestadorId, ClienteId clienteId, ServicoId servicoId,
                         Periodo periodo, Dinheiro valorServico, Dinheiro valorSinal,
                         PoliticaCancelamento politicaAplicada, Instant criadoEm, StatusAgendamento status,
                         String referenciaPagamento) {
        this.id = id;
        this.prestadorId = prestadorId;
        this.clienteId = clienteId;
        this.servicoId = servicoId;
        this.periodo = periodo;
        this.valorServico = valorServico;
        this.valorSinal = valorSinal;
        this.politicaAplicada = politicaAplicada;
        this.criadoEm = criadoEm;
        this.status = status;
        this.referenciaPagamento = referenciaPagamento;
    }

    public static Agendamento criar(AgendamentoId id, PrestadorId prestadorId, ClienteId clienteId,
                                     ServicoId servicoId, Periodo periodo, Dinheiro valorServico,
                                     Dinheiro valorSinal, PoliticaCancelamento politicaAplicada, Instant agora) {
        Objects.requireNonNull(id, "id nao pode ser nulo");
        Objects.requireNonNull(prestadorId, "prestadorId nao pode ser nulo");
        Objects.requireNonNull(clienteId, "clienteId nao pode ser nulo");
        Objects.requireNonNull(servicoId, "servicoId nao pode ser nulo");
        Objects.requireNonNull(periodo, "periodo nao pode ser nulo");
        Objects.requireNonNull(valorServico, "valorServico nao pode ser nulo");
        Objects.requireNonNull(valorSinal, "valorSinal nao pode ser nulo");
        Objects.requireNonNull(politicaAplicada, "politicaAplicada nao pode ser nula");
        Objects.requireNonNull(agora, "agora nao pode ser nulo");
        if (periodo.estaNoPassado(agora)) {
            throw new IllegalArgumentException("nao e possivel agendar em um periodo no passado");
        }
        StatusAgendamento statusInicial = valorSinal.valor().signum() > 0
                ? StatusAgendamento.PENDENTE_PAGAMENTO
                : StatusAgendamento.CONFIRMADO;
        return new Agendamento(id, prestadorId, clienteId, servicoId, periodo, valorServico, valorSinal,
                politicaAplicada, agora, statusInicial, null);
    }

    /**
     * Reconstitui um agendamento a partir de dados ja persistidos, sem reaplicar as regras de criacao
     * (por exemplo, um agendamento concluido no passado nao pode falhar a validacao de "periodo no passado").
     * Uso exclusivo de adapters de persistencia.
     */
    public static Agendamento reconstituir(AgendamentoId id, PrestadorId prestadorId, ClienteId clienteId,
                                            ServicoId servicoId, Periodo periodo, Dinheiro valorServico,
                                            Dinheiro valorSinal, PoliticaCancelamento politicaAplicada,
                                            Instant criadoEm, StatusAgendamento status, String referenciaPagamento) {
        Objects.requireNonNull(id, "id nao pode ser nulo");
        Objects.requireNonNull(prestadorId, "prestadorId nao pode ser nulo");
        Objects.requireNonNull(clienteId, "clienteId nao pode ser nulo");
        Objects.requireNonNull(servicoId, "servicoId nao pode ser nulo");
        Objects.requireNonNull(periodo, "periodo nao pode ser nulo");
        Objects.requireNonNull(valorServico, "valorServico nao pode ser nulo");
        Objects.requireNonNull(valorSinal, "valorSinal nao pode ser nulo");
        Objects.requireNonNull(politicaAplicada, "politicaAplicada nao pode ser nula");
        Objects.requireNonNull(criadoEm, "criadoEm nao pode ser nulo");
        Objects.requireNonNull(status, "status nao pode ser nulo");
        return new Agendamento(id, prestadorId, clienteId, servicoId, periodo, valorServico, valorSinal,
                politicaAplicada, criadoEm, status, referenciaPagamento);
    }

    /**
     * Vincula a referencia externa da cobranca Pix gerada no gateway de pagamento — necessaria
     * mais tarde para executar o estorno no cancelamento. So chamado quando o agendamento exige
     * sinal (ver CriarAgendamentoService); sem sinal, nunca ha o que estornar.
     */
    public void vincularReferenciaPagamento(String referenciaPagamento) {
        Objects.requireNonNull(referenciaPagamento, "referenciaPagamento nao pode ser nula");
        this.referenciaPagamento = referenciaPagamento;
    }

    public Optional<String> referenciaPagamento() {
        return Optional.ofNullable(referenciaPagamento);
    }

    public void confirmar() {
        transicionarPara(StatusAgendamento.CONFIRMADO);
    }

    public void iniciar() {
        transicionarPara(StatusAgendamento.EM_ANDAMENTO);
    }

    public void concluir() {
        transicionarPara(StatusAgendamento.CONCLUIDO);
    }

    public ResultadoCancelamento cancelar(Instant agora) {
        Objects.requireNonNull(agora, "agora nao pode ser nulo");
        transicionarPara(StatusAgendamento.CANCELADO);
        boolean dentroDaJanelaLivre = politicaAplicada.dentroDaJanelaLivre(periodo.inicio(), agora);
        Dinheiro valorRetido = dentroDaJanelaLivre
                ? Dinheiro.ZERO
                : valorSinal.percentual(politicaAplicada.percentualRetido());
        Dinheiro valorReembolsado = valorSinal.subtrair(valorRetido);
        return new ResultadoCancelamento(valorRetido, valorReembolsado);
    }

    public void marcarNoShow(Instant agora) {
        Objects.requireNonNull(agora, "agora nao pode ser nulo");
        if (periodo.inicio().isAfter(agora)) {
            throw new IllegalStateException("nao e possivel marcar no-show antes do horario do agendamento");
        }
        transicionarPara(StatusAgendamento.NO_SHOW);
    }

    public boolean expirarSeNecessario(Instant agora, Duration prazoExpiracao) {
        Objects.requireNonNull(agora, "agora nao pode ser nulo");
        Objects.requireNonNull(prazoExpiracao, "prazoExpiracao nao pode ser nulo");
        if (status != StatusAgendamento.PENDENTE_PAGAMENTO) {
            return false;
        }
        Duration decorrido = Duration.between(criadoEm, agora);
        if (decorrido.compareTo(prazoExpiracao) >= 0) {
            this.status = StatusAgendamento.EXPIRADO;
            return true;
        }
        return false;
    }

    private void transicionarPara(StatusAgendamento novoStatus) {
        if (!status.podeTransicionarPara(novoStatus)) {
            throw new TransicaoDeStatusInvalidaException(status, novoStatus);
        }
        this.status = novoStatus;
    }

    public AgendamentoId id() { return id; }
    public PrestadorId prestadorId() { return prestadorId; }
    public ClienteId clienteId() { return clienteId; }
    public ServicoId servicoId() { return servicoId; }
    public Periodo periodo() { return periodo; }
    public Dinheiro valorServico() { return valorServico; }
    public Dinheiro valorSinal() { return valorSinal; }
    public PoliticaCancelamento politicaAplicada() { return politicaAplicada; }
    public Instant criadoEm() { return criadoEm; }
    public StatusAgendamento status() { return status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Agendamento agendamento)) return false;
        return id.equals(agendamento.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
