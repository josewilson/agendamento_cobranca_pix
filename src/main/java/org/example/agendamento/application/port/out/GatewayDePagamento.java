package org.example.agendamento.application.port.out;

import org.example.agendamento.domain.model.agendamento.AgendamentoId;
import org.example.agendamento.domain.model.cliente.Cliente;
import org.example.agendamento.domain.model.shared.Dinheiro;

public interface GatewayDePagamento {

    CobrancaPix gerarCobrancaPix(AgendamentoId agendamentoId, Cliente cliente, Dinheiro valor);

    void estornar(String referenciaExterna, Dinheiro valor);
}
