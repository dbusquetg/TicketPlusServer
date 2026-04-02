package com.ticketingmaster.ticketplusserver.dto;

import com.ticketingmaster.ticketplusserver.model.Ticket;
import com.ticketingmaster.ticketplusserver.model.TicketStatus;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para un ticket.
 * Expone solo los campos necesarios al cliente,
 * evitando serializar las entidades User completas.
 */
public class TicketResponse {

    private Long          idTicket;
    private LocalDateTime creationDate;
    private String        title;
    private String        description;
    private Integer       priority;
    private String        typology;
    private String        subTypology;
    private TicketStatus  status;
    private String        createdBy;
    private String        agent;

    public TicketResponse() {}

    /**
     * Constructor de conveniencia que mapea directamente desde la entidad.
     */
    public static TicketResponse from(Ticket ticket) {
        TicketResponse dto = new TicketResponse();
        dto.idTicket     = ticket.getIdTicket();
        dto.creationDate = ticket.getCreationDate();
        dto.title        = ticket.getTitle();
        dto.description  = ticket.getDescription();
        dto.priority     = ticket.getPriority();
        dto.typology     = ticket.getTypology();
        dto.subTypology  = ticket.getSubTypology();
        dto.status       = ticket.getStatus();
        dto.createdBy    = ticket.getCreatedBy().getUsername();
        dto.agent        = ticket.getAgent() != null
                           ? ticket.getAgent().getUsername()
                           : null;
        return dto;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────

    public Long getIdTicket()                           { return idTicket; }
    public void setIdTicket(Long idTicket)              { this.idTicket = idTicket; }

    public LocalDateTime getCreationDate()              { return creationDate; }
    public void setCreationDate(LocalDateTime date)     { this.creationDate = date; }

    public String getTitle()                            { return title; }
    public void setTitle(String title)                  { this.title = title; }

    public String getDescription()                      { return description; }
    public void setDescription(String description)      { this.description = description; }

    public Integer getPriority()                        { return priority; }
    public void setPriority(Integer priority)           { this.priority = priority; }

    public String getTypology()                         { return typology; }
    public void setTypology(String typology)            { this.typology = typology; }

    public String getSubTypology()                      { return subTypology; }
    public void setSubTypology(String subTypology)      { this.subTypology = subTypology; }

    public TicketStatus getStatus()                     { return status; }
    public void setStatus(TicketStatus status)          { this.status = status; }

    public String getCreatedBy()                        { return createdBy; }
    public void setCreatedBy(String createdBy)          { this.createdBy = createdBy; }

    public String getAgent()                            { return agent; }
    public void setAgent(String agent)                  { this.agent = agent; }
}
