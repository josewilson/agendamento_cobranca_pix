package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.prestador.PrestadorId;

import java.math.BigDecimal;

public record CadastrarServicoCommand(PrestadorId prestadorId, String nome, long duracaoMinutos,
                                       BigDecimal preco, BigDecimal percentualSinal) {
}
