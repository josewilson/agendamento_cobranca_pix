package org.example.agendamento.adapter.in.web.security;

import org.example.agendamento.domain.model.prestador.Prestador;
import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.shared.DocumentoFiscal;
import org.example.agendamento.domain.model.shared.PoliticaCancelamento;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Constroi uma {@link Authentication} de um {@link PrestadorPrincipal} fake para uso em
 * {@code @WebMvcTest} com {@code SecurityMockMvcRequestPostProcessors.authentication(...)}.
 * Extraido nesta sessao (achado da auditoria de 01/10/2026: esse mesmo helper estava copiado
 * quase literalmente em AgendamentoControllerTest e ServicoControllerTest).
 */
public final class TestAutenticacao {

    private TestAutenticacao() {
    }

    public static Authentication doPrestador(PrestadorId prestadorId) {
        Prestador prestador = new Prestador(prestadorId, "Clinica Teste", "11987654321",
                "clinica@exemplo.com", "hash-fake-de-teste",
                DocumentoFiscal.cnpj("11222333000181"), PoliticaCancelamento.padrao());
        PrestadorPrincipal principal = new PrestadorPrincipal(prestador);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
