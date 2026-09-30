package org.example.agendamento.application.port.out;

import org.example.agendamento.domain.event.EventoDeDominio;

public interface PublicadorDeEventos {

    void publicar(EventoDeDominio evento);
}
