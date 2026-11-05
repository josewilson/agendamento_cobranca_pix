package br.com.josewilson.agendamento.application.port.out;

import br.com.josewilson.agendamento.domain.model.agendamento.AgendamentoId;
import br.com.josewilson.agendamento.domain.model.cliente.Cliente;
import br.com.josewilson.agendamento.domain.model.shared.Dinheiro;

public interface GatewayDePagamento {

    CobrancaPix gerarCobrancaPix(AgendamentoId agendamentoId, Cliente cliente, Dinheiro valor);

    void estornar(String referenciaExterna, Dinheiro valor);
}
