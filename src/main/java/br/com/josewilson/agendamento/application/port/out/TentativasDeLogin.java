package br.com.josewilson.agendamento.application.port.out;

import java.time.Instant;
import java.util.Optional;

/** Contador de falhas de login por email, compartilhavel entre instancias (implementacao JPA). */
public interface TentativasDeLogin {

    /** Ate quando o email esta bloqueado; vazio se nao ha bloqueio registrado. */
    Optional<Instant> bloqueadoAte(String email);

    /** Soma 1 falha (atomico); ao atingir {@code maximo}, grava o bloqueio ate {@code bloqueioAte}. */
    void registrarFalha(String email, int maximo, Instant bloqueioAte);

    void limpar(String email);
}
