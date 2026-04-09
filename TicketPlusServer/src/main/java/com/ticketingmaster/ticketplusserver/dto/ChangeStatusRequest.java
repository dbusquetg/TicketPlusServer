package com.ticketingmaster.ticketplusserver.dto;

/**
 * DTO para cambiar el estado de un ticket.
 *
 * Recibe el estado en texto legible tal como lo envía el cliente:
 *   "Opened", "Pending", "In Progress", "Resolved", "Solved", "Closed"
 *
 * El servicio traduce este texto al enum TicketStatus interno.
 *
 * Ejemplo de JSON recibido:
 * {
 *   "status": "In Progress"
 * }
 * @author david.busquet
 */
public class ChangeStatusRequest {
 
    private String status;
 
    public ChangeStatusRequest() {}
 
    public String getStatus()              { return status; }
    public void setStatus(String status)   { this.status = status; }
}
 