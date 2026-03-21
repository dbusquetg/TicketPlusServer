
package com.ticketingmaster.ticketplusserver.dto;

/**
 * Clase que conforma la respuesta al login, formada por una booleana que devuelve si ha
 * tenido éxito o no en forma de true o false, y un mensaje para dar más contexto a la respuesta.
 * @author David
 */
public class LoginResponse {

    private String token;
    private String role;

    private String username;
    
    /**
     * Constructor que recibe un token, un rol y un nombre de usuario para 
     * devolverlos como respuesta
     */
    public LoginResponse(String token, String role, String username) {
        this.token = token;
        this.role = role;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    
 
}