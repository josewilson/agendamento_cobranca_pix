package br.com.josewilson.agendamento.adapter.in.web.dto;

public record MercadoPagoWebhookRequest(String type, String action, Data data) {

    public record Data(String id) {
    }
}
