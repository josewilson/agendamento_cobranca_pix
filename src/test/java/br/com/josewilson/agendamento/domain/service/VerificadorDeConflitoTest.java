package br.com.josewilson.agendamento.domain.service;

import br.com.josewilson.agendamento.domain.exception.ConflitoDeHorarioException;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoTestDataBuilder.umAgendamento;

class VerificadorDeConflitoTest {

    private static final Instant AGORA = Instant.parse("2026-09-30T12:00:00Z");

    @Test
    void naoDeveLancarExcecaoQuandoNaoHaAgendamentosAtivos() {
        Periodo periodoDesejado = new Periodo(AGORA.plus(Duration.ofDays(1)), AGORA.plus(Duration.ofDays(1)).plus(Duration.ofHours(1)));

        assertThatCode(() -> VerificadorDeConflito.verificarDisponibilidade(periodoDesejado, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void naoDeveLancarExcecaoQuandoPeriodosNaoSeSobrepoem() {
        Periodo periodoExistente = new Periodo(AGORA.plus(Duration.ofDays(1)), AGORA.plus(Duration.ofDays(1)).plus(Duration.ofHours(1)));
        Agendamento existente = umAgendamento().criadoEm(AGORA).comPeriodo(periodoExistente).semSinal().build();

        Periodo periodoDesejado = new Periodo(AGORA.plus(Duration.ofDays(1)).plus(Duration.ofHours(2)),
                AGORA.plus(Duration.ofDays(1)).plus(Duration.ofHours(3)));

        assertThatCode(() -> VerificadorDeConflito.verificarDisponibilidade(periodoDesejado, List.of(existente)))
                .doesNotThrowAnyException();
    }

    @Test
    void deveLancarExcecaoQuandoPeriodosSeSobrepoem() {
        Periodo periodoExistente = new Periodo(AGORA.plus(Duration.ofDays(1)), AGORA.plus(Duration.ofDays(1)).plus(Duration.ofHours(1)));
        Agendamento existente = umAgendamento().criadoEm(AGORA).comPeriodo(periodoExistente).semSinal().build();

        Periodo periodoDesejado = new Periodo(AGORA.plus(Duration.ofDays(1)).plus(Duration.ofMinutes(30)),
                AGORA.plus(Duration.ofDays(1)).plus(Duration.ofHours(2)));

        assertThatThrownBy(() -> VerificadorDeConflito.verificarDisponibilidade(periodoDesejado, List.of(existente)))
                .isInstanceOf(ConflitoDeHorarioException.class);
    }

    @Test
    void naoDeveLancarExcecaoQuandoPeriodosApenasSeTocam() {
        Periodo periodoExistente = new Periodo(AGORA.plus(Duration.ofDays(1)), AGORA.plus(Duration.ofDays(1)).plus(Duration.ofHours(1)));
        Agendamento existente = umAgendamento().criadoEm(AGORA).comPeriodo(periodoExistente).semSinal().build();

        Periodo periodoDesejado = new Periodo(AGORA.plus(Duration.ofDays(1)).plus(Duration.ofHours(1)),
                AGORA.plus(Duration.ofDays(1)).plus(Duration.ofHours(2)));

        assertThatCode(() -> VerificadorDeConflito.verificarDisponibilidade(periodoDesejado, List.of(existente)))
                .doesNotThrowAnyException();
    }
}
