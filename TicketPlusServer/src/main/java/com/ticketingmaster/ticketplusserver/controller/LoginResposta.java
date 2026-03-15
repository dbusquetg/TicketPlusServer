
package com.ticketingmaster.ticketplusserver.controller;

/**
 * Clase que conforma la resposta al login, fomada per una booleana que retorna si ha
 * tingut éxit o no en forma de true o false, i un missatge per donar mes context a la resposta.
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