package org.example.agendamento.adapter.in.scheduler;

import org.example.agendamento.application.port.in.ExpirarReservasPendentesUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class ExpiracaoReservaSchedulerTest {

    private final ExpirarReservasPendentesUseCase useCase = Mockito.mock(ExpirarReservasPendentesUseCase.class);
    private final ExpiracaoReservaScheduler scheduler = new ExpiracaoReservaScheduler(useCase);

    @Test
    void deveExecutarOCasoDeUsoDeExpiracaoAoDisparar() {
        given(useCase.executar()).willReturn(2);

        scheduler.expirarReservasPendentes();

        verify(useCase).executar();
    }

    @Test
    void deveExecutarMesmoQuandoNenhumAgendamentoExpira() {
        given(useCase.executar()).willReturn(0);

        scheduler.expirarReservasPendentes();

        verify(useCase).executar();
    }
}
