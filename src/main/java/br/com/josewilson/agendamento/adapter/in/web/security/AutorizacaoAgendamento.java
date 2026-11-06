package br.com.josewilson.agendamento.adapter.in.web.security;

import br.com.josewilson.agendamento.application.exception.AcessoNaoAutorizadoException;
import br.com.josewilson.agendamento.domain.model.agendamento.Agendamento;
import org.springframework.security.core.Authentication;

/** Regras de quem pode ver/agir sobre um agendamento: o cliente dono ou o prestador dono. */
public final class AutorizacaoAgendamento {

    private AutorizacaoAgendamento() {
    }

    /** Cliente do agendamento ou prestador do agendamento. */
    public static void exigirParticipante(Agendamento agendamento, Authentication authentication) {
        Object principal = authentication.getPrincipal();
        boolean permitido = (principal instanceof ClientePrincipal c && c.clienteId().equals(agendamento.clienteId()))
                || (principal instanceof PrestadorPrincipal p && p.prestadorId().equals(agendamento.prestadorId()));
        if (!permitido) {
            throw new AcessoNaoAutorizadoException("Voce nao tem acesso a este agendamento");
        }
    }

    /** So o prestador dono (ex.: marcar no-show). */
    public static void exigirPrestadorDono(Agendamento agendamento, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof PrestadorPrincipal p)
                || !p.prestadorId().equals(agendamento.prestadorId())) {
            throw new AcessoNaoAutorizadoException("So o prestador do agendamento pode fazer isso");
        }
    }
}
