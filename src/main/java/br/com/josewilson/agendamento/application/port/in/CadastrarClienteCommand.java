package br.com.josewilson.agendamento.application.port.in;

/** {@code canalPreferido} opcional (null = AMBOS): EMAIL, WHATSAPP ou AMBOS. */
public record CadastrarClienteCommand(String nome, String email, String telefone,
                                       String documentoNumero, String documentoTipo, String canalPreferido) {

    public CadastrarClienteCommand(String nome, String email, String telefone,
                                   String documentoNumero, String documentoTipo) {
        this(nome, email, telefone, documentoNumero, documentoTipo, null);
    }
}
