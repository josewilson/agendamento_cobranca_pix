package org.example.agendamento.adapter.in.web.dto;

public record AsaasWebhookRequest(String event, AsaasWebhookPayment payment) {

    public record AsaasWebhookPayment(String id, String externalReference, String status) {
    }
}
