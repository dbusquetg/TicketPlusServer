/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ticketingmaster.ticketplusserver.controller;

/**
 * Clase que conforma la petició de logout, formada per un nom d'usuari i un rol.
 * @author David
 */
public class LogoutRequest {
    private String idSession;

    public LogoutRequest(String idSession) {
        this.idSession = idSession;
    }

    public String getIdSession() {
        return idSession;
    }

    public void setIdSession(String idSession) {
        this.idSession = idSession;
    }
    
}
