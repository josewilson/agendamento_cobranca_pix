package org.example.agendamento.adapter.out.pagamento.memory;

import org.example.agendamento.application.port.out.CobrancaPix;
import org.example.agendamento.application.port.out.GatewayDePagamento;
import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.shared.Dinheiro;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
@Profile("dev")
public class GatewayDePagamentoFake implements GatewayDePagamento {

    @Override
    public CobrancaPix gerarCobrancaPix(AgendamentoId agendamentoId, Cliente cliente, Dinheiro valor) {
        String referencia = "FAKE-" + UUID.randomUUID();
        String copiaECola = "00020126FAKE" + agendamentoId.valor();
        Instant expiraEm = Instant.now().plus(Duration.ofMinutes(30));
        return new CobrancaPix(referencia, "QR-" + referencia, copiaECola, expiraEm);
    }
}
