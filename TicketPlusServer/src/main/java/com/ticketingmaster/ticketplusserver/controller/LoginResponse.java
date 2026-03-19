
package com.ticketingmaster.ticketplusserver.controller;

/**
 * Clase que conforma la resposta al login, fomada per una booleana que retorna si ha
 * tingut éxit o no en forma de true o false, i un missatge per donar mes context a la resposta.
 * @author David
 */
public class LoginResponse {

    private String id_session;
    private String role;

    public LoginResponse(String id_session, String role) {
        this.id_session = id_session;
        this.role = role;
    }

    public String getId_session() {
        return id_session;
    }

    public void setId_session(String id_session) {
        this.id_session = id_session;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    
 
}