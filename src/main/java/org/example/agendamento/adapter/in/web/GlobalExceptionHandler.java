package org.example.agendamento.adapter.in.web;

import org.example.agendamento.adapter.in.web.dto.ErrorResponse;
import org.example.agendamento.application.exception.RecursoNaoEncontradoException;
import org.example.agendamento.domain.exception.ConflitoDeHorarioException;
import org.example.agendamento.domain.exception.TransicaoDeStatusInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse tratarRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
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
