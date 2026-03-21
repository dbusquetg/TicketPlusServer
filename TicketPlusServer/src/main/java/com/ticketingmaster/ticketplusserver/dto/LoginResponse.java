
package com.ticketingmaster.ticketplusserver.dto;

/**
 * Clase que conforma la resposta al login, fomada per una booleana que retorna si ha
 * tingut éxit o no en forma de true o false, i un missatge per donar mes context a la resposta.
 * @author David
 */
public class LoginResponse {

    private String token;
    private String role;

    private String username;

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