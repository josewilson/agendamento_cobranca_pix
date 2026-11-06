package br.com.josewilson.agendamento.application.service;

import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryAgendamentoRepository;
import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryBloqueioRepository;
import br.com.josewilson.agendamento.adapter.out.persistence.memory.InMemoryProfissionalRepository;
import br.com.josewilson.agendamento.application.exception.AcessoNaoAutorizadoException;
import br.com.josewilson.agendamento.domain.exception.ConflitoDeHorarioException;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.cliente.ClienteId;
import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.profissional.Profissional;
import br.com.josewilson.agendamento.domain.model.servico.ServicoId;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;
import br.com.josewilson.agendamento.domain.model.shared.PoliticaCancelamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GerenciarProfissionaisEBloqueiosServiceTest {

    private static final Instant AGORA = Instant.parse("2026-09-30T12:00:00Z");

    private final PrestadorId dono = PrestadorId.novo();
    private InMemoryProfissionalRepository profissionais;
    private InMemoryAgendamentoRepository agendamentos;
    private GerenciarProfissionaisService servicoProfissionais;
    private GerenciarBloqueiosService servicoBloqueios;
    private Profissional ana;

    @BeforeEach
    void setUp() {
        profissionais = new InMemoryProfissionalRepository();
        agendamentos = new InMemoryAgendamentoRepository();
        servicoProfissionais = new GerenciarProfissionaisService(profissionais);
        servicoBloqueios = new GerenciarBloqueiosService(new InMemoryBloqueioRepository(), profissionais,
                agendamentos, new ClockFixo(AGORA));
        ana = servicoProfissionais.cadastrar(dono, "Ana");
    }

    private Periodo periodoEm(int dias, int horaUtc) {
        Instant inicio = AGORA.plus(Duration.ofDays(dias)).truncatedTo(java.time.temporal.ChronoUnit.DAYS)
                .plus(Duration.ofHours(horaUtc));
        return new Periodo(inicio, inicio.plus(Duration.ofHours(1)));
    }

    @Test
    void naoExcluiOUltimoProfissionalMasExcluiQuandoHaOutro() {
        assertThatThrownBy(() -> servicoProfissionais.excluir(dono, ana.id()))
                .isInstanceOf(IllegalStateException.class);

        Profissional beto = servicoProfissionais.cadastrar(dono, "Beto");
        servicoProfissionais.excluir(dono, beto.id());

        assertThat(servicoProfissionais.listar(dono)).extracting(Profissional::nome).containsExactly("Ana");
    }

    @Test
    void soODonoAlteraOuExcluiProfissional() {
        PrestadorId intruso = PrestadorId.novo();

        assertThatThrownBy(() -> servicoProfissionais.atualizar(intruso, ana.id(), "X"))
                .isInstanceOf(AcessoNaoAutorizadoException.class);
        assertThatThrownBy(() -> servicoProfissionais.excluir(intruso, ana.id()))
                .isInstanceOf(AcessoNaoAutorizadoException.class);
    }

    @Test
    void renomeiaProfissional() {
        assertThat(servicoProfissionais.atualizar(dono, ana.id(), "Ana Paula").nome()).isEqualTo("Ana Paula");
    }

    @Test
    void bloqueiaListaERemoveHorario() {
        var bloqueio = servicoBloqueios.bloquear(dono, ana.id(), periodoEm(2, 12), "Almoco");

        assertThat(servicoBloqueios.listar(dono, ana.id())).hasSize(1);
        servicoBloqueios.remover(dono, ana.id(), bloqueio.id());
        assertThat(servicoBloqueios.listar(dono, ana.id())).isEmpty();
    }

    @Test
    void naoBloqueiaPorCimaDeAgendamentoAtivoNemNoPassado() {
        Periodo periodo = periodoEm(2, 15);
        agendamentos.salvar(Agendamento.criar(AgendamentoId.novo(), dono, ClienteId.novo(), ServicoId.novo(), ana.id(),
                periodo, Dinheiro.de("50.00"), Dinheiro.ZERO, PoliticaCancelamento.padrao(), AGORA));

        assertThatThrownBy(() -> servicoBloqueios.bloquear(dono, ana.id(), periodo, null))
                .isInstanceOf(ConflitoDeHorarioException.class);
        assertThatThrownBy(() -> servicoBloqueios.bloquear(dono, ana.id(),
                new Periodo(AGORA.minus(Duration.ofDays(1)), AGORA.minus(Duration.ofHours(23))), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void soODonoMexeNosBloqueios() {
        PrestadorId intruso = PrestadorId.novo();

        assertThatThrownBy(() -> servicoBloqueios.bloquear(intruso, ana.id(), periodoEm(2, 12), null))
                .isInstanceOf(AcessoNaoAutorizadoException.class);
        assertThatThrownBy(() -> servicoBloqueios.listar(intruso, ana.id()))
                .isInstanceOf(AcessoNaoAutorizadoException.class);
    }
}
