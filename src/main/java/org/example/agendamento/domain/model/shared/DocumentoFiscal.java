package org.example.agendamento.domain.model.shared;

import java.util.Objects;

public record DocumentoFiscal(String numero, TipoDocumento tipo) {

    public enum TipoDocumento { CPF, CNPJ }

    public DocumentoFiscal {
        Objects.requireNonNull(numero, "numero nao pode ser nulo");
        Objects.requireNonNull(tipo, "tipo nao pode ser nulo");
        String numeroLimpo = numero.replaceAll("\\D", "");
        boolean valido = switch (tipo) {
            case CPF -> validarCpf(numeroLimpo);
            case CNPJ -> validarCnpj(numeroLimpo);
        };
        if (!valido) {
            throw new IllegalArgumentException(tipo + " invalido: " + numero);
        }
        numero = numeroLimpo;
    }

    public static DocumentoFiscal cpf(String numero) {
        return new DocumentoFiscal(numero, TipoDocumento.CPF);
    }

    public static DocumentoFiscal cnpj(String numero) {
        return new DocumentoFiscal(numero, TipoDocumento.CNPJ);
    }

    private static boolean validarCpf(String cpf) {
        if (cpf.length() != 11 || todosDigitosIguais(cpf)) {
            return false;
        }
        int d1 = calcularDigito(cpf.substring(0, 9), 10);
        int d2 = calcularDigito(cpf.substring(0, 9) + d1, 11);
        return cpf.equals(cpf.substring(0, 9) + d1 + d2);
    }

    private static boolean validarCnpj(String cnpj) {
        if (cnpj.length() != 14 || todosDigitosIguais(cnpj)) {
            return false;
        }
        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int d1 = calcularDigito(cnpj.substring(0, 12), pesos1);
        int d2 = calcularDigito(cnpj.substring(0, 12) + d1, pesos2);
        return cnpj.equals(cnpj.substring(0, 12) + d1 + d2);
    }

    private static int calcularDigito(String base, int pesoInicial) {
        int soma = 0;
        int peso = pesoInicial;
        for (char c : base.toCharArray()) {
            soma += (c - '0') * peso--;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int calcularDigito(String base, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < base.length(); i++) {
            soma += (base.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean todosDigitosIguais(String s) {
        return s.chars().distinct().count() == 1;
    }
}
