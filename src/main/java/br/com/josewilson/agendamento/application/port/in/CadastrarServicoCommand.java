package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;

import java.math.BigDecimal;

public record CadastrarServicoCommand(PrestadorId prestadorId, String nome, long duracaoMinutos,
                                       BigDecimal preco, BigDecimal percentualSinal) {
}
