package com.ticketingmaster.ticketplusserver.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una entrada del hilo de conversación de un ticket.
 * Cada entrada puede ser una pregunta del cliente (T) o una respuesta
 * del gestor (R), formando el historial completo del ticket.
 * @author David Busquet Gimeno
 */
@Entity
@Table(name = "detail_ticket")
public class DetailTicket {

    //  Primary Key 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detail")
    private Long idDetail;

    //  Relación con Ticket 

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ticket", nullable = false)
    private Ticket ticket;

    //  Tipo de entrada 

    @Enumerated(EnumType.STRING)
    @Column(name = "type_detail", nullable = false, length = 1)
    private DetailType typeDetail;

    //  Contenido 

    @Column(name = "content_detail", nullable = false, length = 400)
    private String contentDetail;

    //  Fecha de creación 

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    //  Autor de la entrada 

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author", nullable = false)
    private User author;

    //  Constructors

    public DetailTicket() {}

    public DetailTicket(Ticket ticket, DetailType typeDetail,
                        String contentDetail, User author) {
        this.ticket        = ticket;
        this.typeDetail    = typeDetail;
        this.contentDetail = contentDetail;
        this.author        = author;
        this.creationDate  = LocalDateTime.now();
    }

    //  Getters & Setters 

    public Long getIdDetail()                           { return idDetail; }
    public void setIdDetail(Long idDetail)              { this.idDetail = idDetail; }

    public Ticket getTicket()                           { return ticket; }
    public void setTicket(Ticket ticket)                { this.ticket = ticket; }

    public DetailType getTypeDetail()                   { return typeDetail; }
    public void setTypeDetail(DetailType typeDetail)    { this.typeDetail = typeDetail; }

    public String getContentDetail()                    { return contentDetail; }
    public void setContentDetail(String content)        { this.contentDetail = content; }

    public LocalDateTime getCreationDate()              { return creationDate; }
    public void setCreationDate(LocalDateTime date)     { this.creationDate = date; }

    public User getAuthor()                             { return author; }
    public void setAuthor(User author)                  { this.author = author; }
}
