package br.com.josewilson.agendamento.domain.model.profissional;

import br.com.josewilson.agendamento.domain.model.shared.Periodo;

import java.util.Objects;

/** Horario em que o profissional nao atende (folga, almoco...) — ocupa a agenda sem cliente por tras. */
public class BloqueioDeAgenda {

    private final BloqueioId id;
    private final ProfissionalId profissionalId;
    private final Periodo periodo;
    private final String motivo; // opcional

    public BloqueioDeAgenda(BloqueioId id, ProfissionalId profissionalId, Periodo periodo, String motivo) {
        this.id = Objects.requireNonNull(id, "id nao pode ser nulo");
        this.profissionalId = Objects.requireNonNull(profissionalId, "profissionalId nao pode ser nulo");
        this.periodo = Objects.requireNonNull(periodo, "periodo nao pode ser nulo");
        this.motivo = motivo == null || motivo.isBlank() ? null : motivo;
    }

    public BloqueioId id() { return id; }
    public ProfissionalId profissionalId() { return profissionalId; }
    public Periodo periodo() { return periodo; }
    public String motivo() { return motivo; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof BloqueioDeAgenda b && id.equals(b.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
