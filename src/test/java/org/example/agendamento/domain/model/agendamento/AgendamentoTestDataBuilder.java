package org.example.agendamento.domain.model.agendamento;

import org.example.agendamento.domain.model.cliente.ClienteId;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.ServicoId;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.example.agendamento.domain.model.shared.Periodo;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

public final class AgendamentoTestDataBuilder {

    private final AgendamentoId id = AgendamentoId.novo();
    private final PrestadorId prestadorId = PrestadorId.novo();
    private final ClienteId clienteId = ClienteId.novo();
    private final ServicoId servicoId = ServicoId.novo();
    private Instant agora = Instant.parse("2026-09-30T12:00:00Z");
    private Periodo periodo;
    private Dinheiro valorServico = Dinheiro.de("100.00");
    private Dinheiro valorSinal = Dinheiro.de("30.00");
    private PoliticaCancelamento politica = new PoliticaCancelamento(Duration.ofHours(24), BigDecimal.valueOf(100));

    private AgendamentoTestDataBuilder() {
    }

    public static AgendamentoTestDataBuilder umAgendamento() {
        return new AgendamentoTestDataBuilder();
    }

    public AgendamentoTestDataBuilder comPeriodo(Periodo periodo) {
        this.periodo = periodo;
        return this;
    }

    public AgendamentoTestDataBuilder comValorSinal(Dinheiro valorSinal) {
        this.valorSinal = valorSinal;
        return this;
    }

    public AgendamentoTestDataBuilder comPolitica(PoliticaCancelamento politica) {
        this.politica = politica;
        return this;
    }

    public AgendamentoTestDataBuilder criadoEm(Instant agora) {
        this.agora = agora;
        return this;
    }

    public AgendamentoTestDataBuilder semSinal() {
        this.valorSinal = Dinheiro.ZERO;
        return this;
    }

    public Instant agora() {
        return agora;
    }

    public Agendamento build() {
        Periodo periodoFinal = periodo != null
                ? periodo
                : new Periodo(agora.plus(Duration.ofDays(2)), agora.plus(Duration.ofDays(2)).plus(Duration.ofHours(1)));
        return Agendamento.criar(id, prestadorId, clienteId, servicoId, periodoFinal, valorServico, valorSinal, politica, agora);
    }
}
