package br.com.josewilson.agendamento;

import br.com.josewilson.agendamento.application.port.in.CancelarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.in.ConfirmarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.in.ConsultarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.in.CriarAgendamentoUseCase;
import br.com.josewilson.agendamento.application.port.in.ExpirarReservasPendentesUseCase;
import br.com.josewilson.agendamento.application.port.in.MarcarNoShowUseCase;
import br.com.josewilson.agendamento.application.port.in.ProcessarWebhookPagamentoUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class AgendamentoApplicationTests {

    @Autowired
    private CriarAgendamentoUseCase criarAgendamentoUseCase;
    @Autowired
    private ConfirmarAgendamentoUseCase confirmarAgendamentoUseCase;
    @Autowired
    private CancelarAgendamentoUseCase cancelarAgendamentoUseCase;
    @Autowired
    private MarcarNoShowUseCase marcarNoShowUseCase;
    @Autowired
    private ExpirarReservasPendentesUseCase expirarReservasPendentesUseCase;
    @Autowired
    private ProcessarWebhookPagamentoUseCase processarWebhookPagamentoUseCase;
    @Autowired
    private ConsultarAgendamentoUseCase consultarAgendamentoUseCase;

    @Test
    void contextLoadsComTodosOsCasosDeUsoEmMemoria() {
        assertThat(criarAgendamentoUseCase).isNotNull();
        assertThat(confirmarAgendamentoUseCase).isNotNull();
        assertThat(cancelarAgendamentoUseCase).isNotNull();
        assertThat(marcarNoShowUseCase).isNotNull();
        assertThat(expirarReservasPendentesUseCase).isNotNull();
        assertThat(processarWebhookPagamentoUseCase).isNotNull();
        assertThat(consultarAgendamentoUseCase).isNotNull();
    }
}
