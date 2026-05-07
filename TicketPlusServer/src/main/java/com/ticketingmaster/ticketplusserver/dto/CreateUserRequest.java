package com.ticketingmaster.ticketplusserver.dto;

/**
 * DTO de entrada para crear usuarios desde el panel de administración.
 *
 * Ejemplo JSON:
 * {
 *   "username": "nuevoUsuario",
 *   "password": "admin123",
 *   "role": "USER"
 * }
 *
 * @author David.Busquet
 */
public class CreateUserRequest {

    private String username;
    private String password;
    private String role;

    public CreateUserRequest() {}
    /**
     * Constructor que admite nombre de usuario, la contraseña y el rol en formato String.
     * @param username
     * @param password
     * @param role 
     */
    public CreateUserRequest(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role     = role;
    }

    public String getUsername()                { return username; }
    public void setUsername(String username)   { this.username = username; }

    public String getPassword()                { return password; }
    public void setPassword(String password)   { this.password = password; }

    public String getRole()                    { return role; }
    public void setRole(String role)           { this.role = role; }
}