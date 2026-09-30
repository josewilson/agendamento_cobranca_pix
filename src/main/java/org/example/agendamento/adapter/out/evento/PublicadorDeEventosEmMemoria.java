package org.example.agendamento.adapter.out.evento;

import org.example.agendamento.application.port.out.PublicadorDeEventos;
import org.example.agendamento.domain.event.EventoDeDominio;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Profile("dev")
public class PublicadorDeEventosEmMemoria implements PublicadorDeEventos {

    private final List<EventoDeDominio> eventosPublicados = new CopyOnWriteArrayList<>();

    @Override
    public void publicar(EventoDeDominio evento) {
        eventosPublicados.add(evento);
    }

    public List<EventoDeDominio> eventosPublicados() {
        return Collections.unmodifiableList(eventosPublicados);
    }
}
