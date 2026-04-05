package com.ticketingmaster.ticketplusserver.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un ticket de soporte en el sistema.
 * Un ticket es creado por un cliente (usuario con rol USER) y puede
 * ser asignado a un agente (usuario con rol ADMIN).
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    // ─── Primary Key ──────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ticket")
    private Long idTicket;

    // ─── Campos básicos ───────────────────────────────────────────────────

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "title", nullable = false, length = 150)
    private String title;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length =10)
    private Priority priority;

    @Column(name = "typology", length = 100)
    private String typology;

    @Column(name = "sub_typology", length = 100)
    private String subTypology;

    // ─── Estado ───────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status;

    // ─── Relaciones con User ──────────────────────────────────────────────

    /**
     * Cliente que crea el ticket. Obligatorio.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * Agente asignado al ticket. Puede ser nulo si el ticket
     * está en estado UNASSIGNED.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent")
    private User agent;

    public Ticket() {}

    public Ticket(String title, String description, Priority priority,
                  String typology, String subTypology, User createdBy) {
        this.title        = title;
        this.description  = description;
        this.priority     = priority;
        this.typology     = typology;
        this.subTypology  = subTypology;
        this.createdBy    = createdBy;
        this.creationDate = LocalDateTime.now();
        this.status       = TicketStatus.UNASSIGNED;
    }


    public Long getIdTicket()                        { return idTicket; }
    public void setIdTicket(Long idTicket)           { this.idTicket = idTicket; }

    public LocalDateTime getCreationDate()           { return creationDate; }
    public void setCreationDate(LocalDateTime date)  { this.creationDate = date; }

    public String getTitle()                         { return title; }
    public void setTitle(String title)               { this.title = title; }

    public String getDescription()                   { return description; }
    public void setDescription(String description)   { this.description = description; }

    public Priority getPriority()                     { return priority; }
    public void setPriority(Priority priority)        { this.priority = priority; }

    public String getTypology()                      { return typology; }
    public void setTypology(String typology)         { this.typology = typology; }

    public String getSubTypology()                   { return subTypology; }
    public void setSubTypology(String subTypology)   { this.subTypology = subTypology; }

    public TicketStatus getStatus()                  { return status; }
    public void setStatus(TicketStatus status)       { this.status = status; }

    public User getCreatedBy()                       { return createdBy; }
    public void setCreatedBy(User createdBy)         { this.createdBy = createdBy; }

    public User getAgent()                           { return agent; }
    public void setAgent(User agent)                 { this.agent = agent; }
}
