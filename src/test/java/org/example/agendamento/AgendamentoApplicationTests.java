package org.example.agendamento;

import org.example.agendamento.application.port.in.CancelarAgendamentoUseCase;
import org.example.agendamento.application.port.in.ConfirmarAgendamentoUseCase;
import org.example.agendamento.application.port.in.CriarAgendamentoUseCase;
import org.example.agendamento.application.port.in.ExpirarReservasPendentesUseCase;
import org.example.agendamento.application.port.in.MarcarNoShowUseCase;
import org.example.agendamento.application.port.in.ProcessarWebhookPagamentoUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
                "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
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

    @Test
    void contextLoadsComTodosOsCasosDeUsoEmMemoria() {
        assertThat(criarAgendamentoUseCase).isNotNull();
        assertThat(confirmarAgendamentoUseCase).isNotNull();
        assertThat(cancelarAgendamentoUseCase).isNotNull();
        assertThat(marcarNoShowUseCase).isNotNull();
        assertThat(expirarReservasPendentesUseCase).isNotNull();
        assertThat(processarWebhookPagamentoUseCase).isNotNull();
    }
}
