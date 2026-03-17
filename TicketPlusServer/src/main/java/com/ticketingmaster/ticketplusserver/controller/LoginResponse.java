
package com.ticketingmaster.ticketplusserver.controller;

/**
 * Clase que conforma la resposta al login, fomada per una booleana que retorna si ha
 * tingut éxit o no en forma de true o false, i un missatge per donar mes context a la resposta.
 * @author David
 */
public class LoginResponse {

    private boolean success;
    private String message;

    public LoginResponse(boolean exit, String message) {
        this.success = exit;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    
 
}