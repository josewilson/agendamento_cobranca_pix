package org.example.agendamento.adapter.in.web.logging;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void geraNovoIdQuandoRequisicaoNaoTrazHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String[] idDuranteOChain = new String[1];

        filter.doFilter(request, response, (req, res) -> idDuranteOChain[0] = MDC.get(CorrelationIdFilter.MDC_KEY));

        assertThat(idDuranteOChain[0]).isNotBlank();
        assertThat(response.getHeader(CorrelationIdFilter.HEADER_REQUEST_ID)).isEqualTo(idDuranteOChain[0]);
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void reaproveitaIdJaEnviadoPeloChamador() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER_REQUEST_ID, "id-externo-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        assertThat(response.getHeader(CorrelationIdFilter.HEADER_REQUEST_ID)).isEqualTo("id-externo-123");
    }

    @Test
    void limpaOMdcMesmoSeOChainLancarExcecao() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                filter.doFilter(request, response, (req, res) -> {
                    throw new IllegalStateException("falha simulada");
                })).isInstanceOf(IllegalStateException.class);

        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}
