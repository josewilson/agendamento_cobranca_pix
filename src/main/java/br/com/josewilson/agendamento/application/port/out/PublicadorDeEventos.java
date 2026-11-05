package br.com.josewilson.agendamento.application.port.out;

import br.com.josewilson.agendamento.domain.event.EventoDeDominio;

public interface PublicadorDeEventos {

    void publicar(EventoDeDominio evento);
}
