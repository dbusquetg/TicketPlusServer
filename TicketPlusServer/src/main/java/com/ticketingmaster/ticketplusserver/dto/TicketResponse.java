package com.ticketingmaster.ticketplusserver.dto;

import com.ticketingmaster.ticketplusserver.model.Priority;
import com.ticketingmaster.ticketplusserver.model.Ticket;
import com.ticketingmaster.ticketplusserver.model.TicketStatus;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para un ticket.
 *
 * Campos generados por el servidor:
 *   ref        → "INC-{id}"
 *   status     → traducido del enum interno a texto legible
 *   createdBy  → username extraído del JWT
 *   createdAt  → timestamp de creación
 *   resolvedAt → timestamp de cierre (null si el ticket está abierto)
 *
 * Mapeo de status:
 *   UNASSIGNED  → "Opened"
 *   PENDING     → "Pending"
 *   IN_PROGRESS → "In Progress"
 *   RESOLVED    → "Resolved"
 *   SOLVED      → "Solved"
 *   CLOSED      → "Closed"
 */
public class TicketResponse {

    private Long          id;
    private String        ref;
    private String        title;
    private String        description;
    private Priority      priority;
    private String        status;
    private String        createdBy;
    private String        agent;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public TicketResponse() {}

    /**
     * Mapea directamente desde la entidad Ticket.
     */
    public static TicketResponse from(Ticket ticket) {
        TicketResponse dto = new TicketResponse();
        dto.id         = ticket.getIdTicket();
        dto.ref        = "INC-" + ticket.getIdTicket();
        dto.title      = ticket.getTitle();
        dto.description= ticket.getDescription();
        dto.priority   = ticket.getPriority();
        dto.status     = mapStatus(ticket.getStatus());
        dto.createdBy  = ticket.getCreatedBy().getUsername();
        dto.agent      = ticket.getAgent() != null
                         ? ticket.getAgent().getUsername()
                         : null;
        dto.createdAt  = ticket.getCreationDate();
        dto.resolvedAt = ticket.getClosedDate();
        return dto;
    }

    /**
     * Traduce el enum interno TicketStatus a texto legible para el cliente.
     */
    private static String mapStatus(TicketStatus status) {
        return switch (status) {
            case UNASSIGNED  -> "Opened";
            case PENDING     -> "Pending";
            case IN_PROGRESS -> "In Progress";
            case RESOLVED    -> "Resolved";
            case SOLVED      -> "Solved";
            case CLOSED      -> "Closed";
        };
    }

    // ─── Getters & Setters ────────────────────────────────────────────────

    public Long getId()                               { return id; }
    public void setId(Long id)                        { this.id = id; }

    public String getRef()                            { return ref; }
    public void setRef(String ref)                    { this.ref = ref; }

    public String getTitle()                          { return title; }
    public void setTitle(String title)                { this.title = title; }

    public String getDescription()                    { return description; }
    public void setDescription(String description)    { this.description = description; }

    public Priority getPriority()                     { return priority; }
    public void setPriority(Priority priority)        { this.priority = priority; }

    public String getStatus()                         { return status; }
    public void setStatus(String status)              { this.status = status; }

    public String getCreatedBy()                      { return createdBy; }
    public void setCreatedBy(String createdBy)        { this.createdBy = createdBy; }

    public String getAgent()                          { return agent; }
    public void setAgent(String agent)                { this.agent = agent; }

    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt()               { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt){ this.resolvedAt = resolvedAt; }
}