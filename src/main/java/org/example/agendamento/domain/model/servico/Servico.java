package org.example.agendamento.domain.model.servico;

import org.example.agendamento.domain.model.prestador.PrestadorId;
import org.example.agendamento.domain.model.shared.Dinheiro;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;

public class Servico {

    private final ServicoId id;
    private final PrestadorId prestadorId;
    private final String nome;
    private final Duration duracao;
    private final Dinheiro preco;
    private final BigDecimal percentualSinal;

    public Servico(ServicoId id, PrestadorId prestadorId, String nome, Duration duracao, Dinheiro preco, BigDecimal percentualSinal) {
        this.id = Objects.requireNonNull(id, "id nao pode ser nulo");
        this.prestadorId = Objects.requireNonNull(prestadorId, "prestadorId nao pode ser nulo");
        this.nome = validarNome(nome);
        this.duracao = validarDuracao(duracao);
        this.preco = Objects.requireNonNull(preco, "preco nao pode ser nulo");
        this.percentualSinal = validarPercentualSinal(percentualSinal);
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome nao pode ser vazio");
        }
        return nome;
    }

    private static Duration validarDuracao(Duration duracao) {
        Objects.requireNonNull(duracao, "duracao nao pode ser nula");
        if (duracao.isZero() || duracao.isNegative()) {
            throw new IllegalArgumentException("duracao deve ser positiva");
        }
        return duracao;
    }

    private static BigDecimal validarPercentualSinal(BigDecimal percentual) {
        Objects.requireNonNull(percentual, "percentualSinal nao pode ser nulo");
        if (percentual.compareTo(BigDecimal.ZERO) < 0 || percentual.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("percentualSinal deve estar entre 0 e 100");
        }
        return percentual;
    }

    public boolean exigeSinal() {
        return percentualSinal.compareTo(BigDecimal.ZERO) > 0;
    }

    public Dinheiro calcularSinal() {
        return preco.percentual(percentualSinal);
    }

    public ServicoId id() { return id; }
    public PrestadorId prestadorId() { return prestadorId; }
    public String nome() { return nome; }
    public Duration duracao() { return duracao; }
    public Dinheiro preco() { return preco; }
    public BigDecimal percentualSinal() { return percentualSinal; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Servico servico)) return false;
        return id.equals(servico.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
