package com.ticketingmaster.ticketplusserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para añadir una nueva entrada al hilo de un ticket.
 * El tipo (T/R) y el autor se derivan del rol del usuario autenticado
 * en el servicio, no los envía el cliente directamente.
 */
public class DetailTicketRequest {

    @JsonProperty("content")
    private String contentDetail;

    public DetailTicketRequest() {}

    public String getContentDetail()                { return contentDetail; }
    public void setContentDetail(String content)    { this.contentDetail = content; }
}
