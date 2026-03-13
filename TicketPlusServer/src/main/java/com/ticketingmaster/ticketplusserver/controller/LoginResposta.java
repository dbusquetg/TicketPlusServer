/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ticketingmaster.ticketplusserver.controller;

/**
 *
 * @author David
 */
public class LoginResposta {

    private boolean exit;
    private String missatge;

    public LoginResposta(boolean exit, String missatge) {
        this.exit = exit;
        this.missatge = missatge;
    }

    public boolean isExit() {
        return exit;
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    public String getMissatge() {
        return missatge;
    }

    public void setMissatge(String missatge) {
        this.missatge = missatge;
    }

 
}