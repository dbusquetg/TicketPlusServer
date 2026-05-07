package com.ticketingmaster.ticketplusserver.dto;

import com.ticketingmaster.ticketplusserver.model.User;

/**
 * DTO de salida para usuarios.
 * Nunca expone passwordHash.
 *
 * Ejemplo JSON:
 * {
 *   "id": 1,
 *   "username": "admin",
 *   "role": "ADMIN",
 *   "active": true
 * }
 *
 * @author TicketPlus
 */
public class UserResponse {

    private Long    id;
    private String  username;
    private String  role;
    private boolean active;

    public UserResponse() {}
    /**
     * Constructor que recibe un objeto usuario en forma de User, y
     * obtiene la información de los mismos.
     * @param user
     * @return 
     */
    public static UserResponse from(User user) {
        UserResponse dto = new UserResponse();
        dto.id       = user.getId();
        dto.username = user.getUsername();
        dto.role     = user.getRole().name();
        dto.active   = user.isActive();
        return dto;
    }

    public Long getId()                      { return id; }
    public void setId(Long id)               { this.id = id; }

    public String getUsername()              { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole()                  { return role; }
    public void setRole(String role)         { this.role = role; }

    public boolean isActive()                { return active; }
    public void setActive(boolean active)    { this.active = active; }
}