package com.ticketingmaster.ticketplusserver.dto;
import com.ticketingmaster.ticketplusserver.model.DetailTicket;
 
import java.time.LocalDateTime;
 
/**
 * DTO de respuesta para el endpoint de comentarios.
 * Representa una entrada de DetailTicket con el formato
 * esperado por el cliente en POST /api/tickets/{id}/comments.
 *
 * Ejemplo de JSON devuelto:
 * {
 *   "id": 1,
 *   "ticketRef": "INC-1",
 *   "ticketTitle": "Mi PC no enciende",
 *   "author": "admin",
 *   "content": "Puedes probar ahora Maria?",
 *   "createdAt": "2026-03-30T20:10:00"
 * }
 * @author David.Busquet
 */
public class CommentResponse {
 
    private Long          id;
    private String        ticketRef;
    private String        ticketTitle;
    private String        author;
    private String        content;
    private LocalDateTime createdAt;
 
    public CommentResponse() {}
 
    /**
     * Mapea directamente desde la entidad DetailTicket.
     */
    public static CommentResponse from(DetailTicket detail) {
        CommentResponse dto = new CommentResponse();
        dto.id          = detail.getIdDetail();
        dto.ticketRef   = "INC-" + detail.getTicket().getIdTicket();
        dto.ticketTitle = detail.getTicket().getTitle();
        dto.author      = detail.getAuthor().getUsername();
        dto.content     = detail.getContentDetail();
        dto.createdAt   = detail.getCreationDate();
        return dto;
    }
 
    public Long getId()                               { return id; }
    public void setId(Long id)                        { this.id = id; }
 
    public String getTicketRef()                      { return ticketRef; }
    public void setTicketRef(String ticketRef)        { this.ticketRef = ticketRef; }
 
    public String getTicketTitle()                    { return ticketTitle; }
    public void setTicketTitle(String ticketTitle)    { this.ticketTitle = ticketTitle; }
 
    public String getAuthor()                         { return author; }
    public void setAuthor(String author)              { this.author = author; }
 
    public String getContent()                        { return content; }
    public void setContent(String content)            { this.content = content; }
 
    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}