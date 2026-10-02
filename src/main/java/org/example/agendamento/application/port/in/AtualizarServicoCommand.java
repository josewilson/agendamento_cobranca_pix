package org.example.agendamento.application.port.in;

import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.servico.ServicoId;

import java.math.BigDecimal;

public record AtualizarServicoCommand(ServicoId servicoId, PrestadorId prestadorAutenticado, String nome,
                                       long duracaoMinutos, BigDecimal preco, BigDecimal percentualSinal) {
}
