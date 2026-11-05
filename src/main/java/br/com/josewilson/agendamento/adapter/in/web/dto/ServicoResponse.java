package br.com.josewilson.agendamento.adapter.in.web.dto;

import br.com.josewilson.agendamento.domain.model.servico.Servico;

import java.math.BigDecimal;
import java.util.UUID;

public record ServicoResponse(UUID id, UUID prestadorId, String nome, long duracaoMinutos,
                               BigDecimal preco, BigDecimal percentualSinal) {

    public static ServicoResponse de(Servico servico) {
        return new ServicoResponse(
                servico.id().valor(),
                servico.prestadorId().valor(),
                servico.nome(),
                servico.duracao().toMinutes(),
                servico.preco().valor(),
                servico.percentualSinal());
    }
}
