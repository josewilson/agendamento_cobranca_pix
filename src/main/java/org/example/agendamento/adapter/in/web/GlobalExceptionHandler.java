package org.example.agendamento.adapter.in.web;

import org.example.agendamento.adapter.in.web.dto.ErrorResponse;
import org.example.agendamento.adapter.in.web.security.LoginBloqueadoException;
import org.example.agendamento.application.exception.AcessoNaoAutorizadoException;
import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.domain.exception.ConflitoDeHorarioException;
import org.example.agendamento.domain.exception.TransicaoDeStatusInvalidaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse tratarRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(AcessoNaoAutorizadoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse tratarAcessoNaoAutorizado(AcessoNaoAutorizadoException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(ConflitoDeHorarioException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse tratarConflitoDeHorario(ConflitoDeHorarioException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(TransicaoDeStatusInvalidaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse tratarTransicaoInvalida(TransicaoDeStatusInvalidaException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    /**
     * Captura violacao de constraint unica (documento ou email duplicado — uk_prestador_documento,
     * uk_cliente_documento, uk_prestador_email) antes que vire um 500 cru. A mensagem e generica
     * de proposito: a causa exata (qual coluna, qual valor) e detalhe de implementacao do banco,
     * nao algo que a API deveria vazar pro cliente.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse tratarViolacaoDeIntegridade(DataIntegrityViolationException ex) {
        return new ErrorResponse("Ja existe um cadastro com esses dados (documento ou email duplicado)");
    }

    @ExceptionHandler(LoginBloqueadoException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ErrorResponse tratarLoginBloqueado(LoginBloqueadoException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    /**
     * Falha de comunicacao com um gateway externo (Asaas/Mercado Pago/Google Calendar/Twilio) —
     * HTTP 401/403/5xx do gateway, timeout, conexao recusada, etc. Sem isso, qualquer uma dessas
     * falhas (ex.: ASAAS_API_KEY nao configurada no ambiente, retornando 401 da API do Asaas)
     * vira um 500 cru sem explicacao nenhuma pro chamador. 502 Bad Gateway sinaliza corretamente
     * que o problema e numa dependencia externa, nao na propria aplicacao; a mensagem original
     * do gateway fica so no log (pode conter detalhe de implementacao da integracao), nunca na
     * resposta.
     */
    @ExceptionHandler(RestClientException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse tratarFalhaDeGatewayExterno(RestClientException ex) {
        log.error("Falha ao comunicar com gateway externo: {}", ex.getMessage(), ex);
        return new ErrorResponse("Nao foi possivel concluir a operacao: falha ao comunicar com um servico externo (pagamento, calendario ou notificacao). Tente novamente em instantes.");
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse tratarArgumentoInvalido(RuntimeException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse tratarValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return new ErrorResponse(mensagem);
    }
}
