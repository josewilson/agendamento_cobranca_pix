package br.com.josewilson.agendamento.application.port.in;

import br.com.josewilson.agendamento.domain.model.prestador.PrestadorId;
import br.com.josewilson.agendamento.domain.model.servico.ServicoId;

import java.math.BigDecimal;

public record AtualizarServicoCommand(ServicoId servicoId, PrestadorId prestadorAutenticado, String nome,
                                       long duracaoMinutos, BigDecimal preco, BigDecimal percentualSinal) {
}
