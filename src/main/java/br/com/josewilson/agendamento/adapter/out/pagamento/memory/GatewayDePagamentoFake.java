package br.com.josewilson.agendamento.adapter.out.pagamento.memory;

import br.com.josewilson.agendamento.application.port.out.CobrancaPix;
import br.com.josewilson.agendamento.application.port.out.GatewayDePagamento;
import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("dev")
public class GatewayDePagamentoFake implements GatewayDePagamento {

    private final Map<String, Dinheiro> estornosRealizados = new ConcurrentHashMap<>();

    @Override
    public CobrancaPix gerarCobrancaPix(AgendamentoId agendamentoId, Cliente cliente, Dinheiro valor) {
        String referencia = "FAKE-" + UUID.randomUUID();
        String copiaECola = "00020126FAKE" + agendamentoId.valor();
        Instant expiraEm = Instant.now().plus(Duration.ofMinutes(30));
        return new CobrancaPix(referencia, "QR-" + referencia, copiaECola, expiraEm);
    }

    @Override
    public void estornar(String referenciaExterna, Dinheiro valor) {
        estornosRealizados.put(referenciaExterna, valor);
    }

    public Map<String, Dinheiro> estornosRealizados() {
        return estornosRealizados;
    }
}
