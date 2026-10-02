package org.example.agendamento.adapter.in.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Forca a resolucao do CsrfToken em toda requisicao, para o cookie XSRF-TOKEN ser gravado mesmo
 * em metodos seguros (GET) — por padrao o Spring Security so grava o cookie na primeira vez que
 * algo chama {@code csrfToken.getToken()}, o que nunca aconteceria sozinho numa API sem views
 * server-side. O frontend conta com isso: a primeira chamada de qualquer pagina (`GET /api/auth/me`,
 * feita pelo `AuthProvider` no mount) ja deixa o cookie pronto para as chamadas de escrita seguintes.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
