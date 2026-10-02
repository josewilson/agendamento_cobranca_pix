package org.example.agendamento.adapter.in.web.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Propaga um id de correlacao por todas as linhas de log de uma mesma requisicao (via MDC),
 * reaproveitando o header X-Request-Id se o chamador ja mandou um (util num ambiente com proxy
 * reverso/load balancer que gera esse header), ou gerando um novo caso contrario. Devolve o id
 * usado no mesmo header da resposta, pra quem chamou conseguir correlacionar com o proprio log.
 * Sem isso, nao havia nenhum jeito de juntar as linhas de log de uma unica requisicao numa
 * aplicacao com mais de uma instancia (achado de auditoria de observabilidade, 02/10/2026).
 * {@code @Order(HIGHEST_PRECEDENCE)} garante que o id ja esta no MDC antes de qualquer outro
 * filtro (inclusive os do Spring Security) logar algo sobre a requisicao.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = request.getHeader(HEADER_REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, requestId);
        response.setHeader(HEADER_REQUEST_ID, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
