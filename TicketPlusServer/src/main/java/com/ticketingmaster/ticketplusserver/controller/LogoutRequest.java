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
    private String user;
    private String role;

    public LogoutRequest(String user, String role) {
        this.user = user;
        this.role = role;
    }
    
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
