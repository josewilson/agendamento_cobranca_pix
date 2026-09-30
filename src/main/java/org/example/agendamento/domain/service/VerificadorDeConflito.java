package org.example.agendamento.domain.service;

import org.example.agendamento.domain.exception.ConflitoDeHorarioException;
import org.example.agendamento.domain.model.agendamento.Agendamento;
import org.example.agendamento.domain.model.shared.Periodo;

import java.util.List;

public final class VerificadorDeConflito {

    private VerificadorDeConflito() {
    }

    public static void verificarDisponibilidade(Periodo periodoDesejado, List<Agendamento> agendamentosAtivos) {
        boolean conflito = agendamentosAtivos.stream()
                .anyMatch(agendamento -> agendamento.periodo().sobrepoe(periodoDesejado));
        if (conflito) {
            throw new ConflitoDeHorarioException(
                    "O periodo " + periodoDesejado + " conflita com um agendamento existente");
        }
    }
}
