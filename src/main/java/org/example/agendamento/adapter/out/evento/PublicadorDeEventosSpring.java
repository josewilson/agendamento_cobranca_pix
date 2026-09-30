package org.example.agendamento.adapter.out.evento;

import org.example.agendamento.application.port.out.PublicadorDeEventos;
import org.example.agendamento.domain.event.EventoDeDominio;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Publica eventos de dominio atraves do ApplicationEventPublisher do Spring, permitindo
 * que listeners (@EventListener) reajam a eles — por exemplo, NotificacaoEventListener.
 * Os eventos continuam sendo records puros do dominio; so este adapter conhece Spring.
 */
@Component
@Profile("!dev")
public class PublicadorDeEventosSpring implements PublicadorDeEventos {

    private final ApplicationEventPublisher applicationEventPublisher;

    public PublicadorDeEventosSpring(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publicar(EventoDeDominio evento) {
        applicationEventPublisher.publishEvent(evento);
    }
}
