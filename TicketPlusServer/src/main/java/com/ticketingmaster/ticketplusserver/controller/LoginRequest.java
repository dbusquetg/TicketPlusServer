
package com.ticketingmaster.ticketplusserver.controller;

/**
 * Clase que conforma la petició de login, formada per un nom d'usuari i una contrasenya.
 * @author David
 */
public class LoginRequest {

    private String user;
    private String password;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
   
  
}