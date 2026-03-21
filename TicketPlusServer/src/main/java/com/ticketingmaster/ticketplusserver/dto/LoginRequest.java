
package com.ticketingmaster.ticketplusserver.dto;

/**
 * Clase que conforma la petición de login, formada por un nombre de usuario y contraseña.
 * @author David
 */
public class LoginRequest {

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
   
  
}