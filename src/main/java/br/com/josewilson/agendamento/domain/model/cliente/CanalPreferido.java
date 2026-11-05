package br.com.josewilson.agendamento.domain.model.cliente;

/** Por quais canais o cliente quer receber avisos de agendamento. Padrao: ambos. */
public enum CanalPreferido {
    AMBOS, EMAIL, WHATSAPP;

    public boolean usaEmail() {
        return this != WHATSAPP;
    }

    public boolean usaWhatsapp() {
        return this != EMAIL;
    }
}
