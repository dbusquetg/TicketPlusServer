package com.ticketingmaster.ticketplusserver.dto;

import com.ticketingmaster.ticketplusserver.model.DetailTicket;
import com.ticketingmaster.ticketplusserver.model.DetailType;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para una entrada del hilo de conversación.
 * Expone solo los campos necesarios al cliente, evitando
 * serializar las entidades completas.
 */
public class DetailTicketResponse {

    private Long          idDetail;
    private Long          idTicket;
    private DetailType    typeDetail;
    private String        contentDetail;
    private LocalDateTime creationDate;
    private String        author;

    public DetailTicketResponse() {}

    /**
     * Constructor de conveniencia que mapea directamente desde la entidad.
     */
    public static DetailTicketResponse from(DetailTicket detail) {
        DetailTicketResponse dto = new DetailTicketResponse();
        dto.idDetail      = detail.getIdDetail();
        dto.idTicket      = detail.getTicket().getIdTicket();
        dto.typeDetail    = detail.getTypeDetail();
        dto.contentDetail = detail.getContentDetail();
        dto.creationDate  = detail.getCreationDate();
        dto.author        = detail.getAuthor().getUsername();
        return dto;
    }

    public Long getIdDetail()                           { return idDetail; }
    public void setIdDetail(Long idDetail)              { this.idDetail = idDetail; }

    public Long getIdTicket()                           { return idTicket; }
    public void setIdTicket(Long idTicket)              { this.idTicket = idTicket; }

    public DetailType getTypeDetail()                   { return typeDetail; }
    public void setTypeDetail(DetailType typeDetail)    { this.typeDetail = typeDetail; }

    public String getContentDetail()                    { return contentDetail; }
    public void setContentDetail(String content)        { this.contentDetail = content; }

    public LocalDateTime getCreationDate()              { return creationDate; }
    public void setCreationDate(LocalDateTime date)     { this.creationDate = date; }

    public String getAuthor()                           { return author; }
    public void setAuthor(String author)                { this.author = author; }
}
