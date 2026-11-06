package br.com.josewilson.agendamento.application.port.in;

/**
 * {@code canalPreferido} opcional (null = AMBOS). {@code senha} em texto puro so trafega ate o
 * servico, que gera o hash; null = cliente sem login (so usado por testes/dados legados — a API
 * exige senha).
 */
public record CadastrarClienteCommand(String nome, String email, String telefone,
                                       String documentoNumero, String documentoTipo,
                                       String canalPreferido, String senha) {

    public CadastrarClienteCommand(String nome, String email, String telefone,
                                   String documentoNumero, String documentoTipo, String canalPreferido) {
        this(nome, email, telefone, documentoNumero, documentoTipo, canalPreferido, null);
    }

    public CadastrarClienteCommand(String nome, String email, String telefone,
                                   String documentoNumero, String documentoTipo) {
        this(nome, email, telefone, documentoNumero, documentoTipo, null, null);
    }
}
