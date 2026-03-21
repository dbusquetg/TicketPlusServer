
package com.ticketingmaster.ticketplusserver.dto;

/**
 * Clase que conforma la petició de login, formada per un nom d'usuari i una contrasenya.
 * @author David
 */
public class LoginRequest {

    private String name;
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
   
  
}