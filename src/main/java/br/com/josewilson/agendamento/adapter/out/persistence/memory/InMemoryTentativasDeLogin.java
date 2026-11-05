package br.com.josewilson.agendamento.adapter.out.persistence.memory;

import br.com.josewilson.agendamento.application.port.out.TentativasDeLogin;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("dev")
public class InMemoryTentativasDeLogin implements TentativasDeLogin {

    private record Registro(int falhas, Instant bloqueadoAte) {
    }

    private final ConcurrentHashMap<String, Registro> registros = new ConcurrentHashMap<>();

    @Override
    public Optional<Instant> bloqueadoAte(String email) {
        return Optional.ofNullable(registros.get(email)).map(Registro::bloqueadoAte);
    }

    @Override
    public void registrarFalha(String email, int maximo, Instant bloqueioAte) {
        registros.compute(email, (chave, atual) -> {
            int falhas = (atual == null ? 0 : atual.falhas()) + 1;
            return new Registro(falhas, falhas >= maximo ? bloqueioAte : null);
        });
    }

    @Override
    public void limpar(String email) {
        registros.remove(email);
    }
}
