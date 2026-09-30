package org.example.agendamento.domain.model.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Dinheiro(BigDecimal valor) implements Comparable<Dinheiro> {

    public static final Dinheiro ZERO = Dinheiro.de(BigDecimal.ZERO);

    public Dinheiro {
        Objects.requireNonNull(valor, "valor nao pode ser nulo");
        if (valor.signum() < 0) {
            throw new IllegalArgumentException("valor nao pode ser negativo");
        }
        valor = valor.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Dinheiro de(BigDecimal valor) {
        return new Dinheiro(valor);
    }

    public static Dinheiro de(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    public Dinheiro somar(Dinheiro outro) {
        Objects.requireNonNull(outro, "outro nao pode ser nulo");
        return new Dinheiro(this.valor.add(outro.valor));
    }

    public Dinheiro subtrair(Dinheiro outro) {
        Objects.requireNonNull(outro, "outro nao pode ser nulo");
        return new Dinheiro(this.valor.subtract(outro.valor));
    }

    public Dinheiro percentual(BigDecimal percentual) {
        Objects.requireNonNull(percentual, "percentual nao pode ser nulo");
        BigDecimal resultado = this.valor.multiply(percentual)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN);
        return new Dinheiro(resultado);
    }

    public boolean maiorQue(Dinheiro outro) {
        return this.compareTo(outro) > 0;
    }

    @Override
    public int compareTo(Dinheiro outro) {
        return this.valor.compareTo(outro.valor);
    }
}
