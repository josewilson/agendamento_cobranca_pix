package br.com.josewilson.agendamento.domain.service;

import br.com.josewilson.agendamento.domain.exception.ConflitoDeHorarioException;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import br.com.josewilson.agendamento.domain.model.profissional.BloqueioDeAgenda;
import br.com.josewilson.agendamento.domain.model.shared.Periodo;

import java.util.List;

public final class VerificadorDeConflito {

    private VerificadorDeConflito() {
    }

    public static void verificarDisponibilidade(Periodo periodoDesejado, List<Agendamento> agendamentosAtivos) {
        verificarDisponibilidade(periodoDesejado, agendamentosAtivos, List.of());
    }

    /** Conflita com outro agendamento ativo OU com um horario bloqueado do mesmo profissional. */
    public static void verificarDisponibilidade(Periodo periodoDesejado, List<Agendamento> agendamentosAtivos,
                                                List<BloqueioDeAgenda> bloqueios) {
        boolean conflito = agendamentosAtivos.stream()
                .anyMatch(agendamento -> agendamento.periodo().sobrepoe(periodoDesejado));
        if (conflito) {
            throw new ConflitoDeHorarioException(
                    "O periodo " + periodoDesejado + " conflita com um agendamento existente");
        }
        boolean bloqueado = bloqueios.stream().anyMatch(b -> b.periodo().sobrepoe(periodoDesejado));
        if (bloqueado) {
            throw new ConflitoDeHorarioException("O periodo " + periodoDesejado + " esta bloqueado na agenda do profissional");
        }
    }
}
