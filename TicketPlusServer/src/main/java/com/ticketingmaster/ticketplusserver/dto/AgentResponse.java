package com.ticketingmaster.ticketplusserver.dto;


import com.ticketingmaster.ticketplusserver.model.User;

/**
 * DTO de respuesta para un agente (usuario con rol ADMIN).
 * Expone solo id, username y role — nunca passwordHash ni active.
 *
 * Ejemplo de JSON devuelto:
 * {
 *   "id": 1,
 *   "username": "admin",
 *   "role": "ADMIN"
 * }
 * @author David.Busquet
 */
public class AgentResponse {

    private Long   id;
    private String username;
    private String role;

    public AgentResponse() {}

    /**
     * Mapea directamente desde la entidad User.
     */
    public static AgentResponse from(User user) {
        AgentResponse dto = new AgentResponse();
        dto.id       = user.getId();
        dto.username = user.getUsername();
        dto.role     = user.getRole().name();
        return dto;
    }

    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }

    public String getUsername()            { return username; }
    public void setUsername(String u)      { this.username = u; }

    public String getRole()                { return role; }
    public void setRole(String role)       { this.role = role; }
}