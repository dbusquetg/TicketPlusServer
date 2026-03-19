
package com.ticketingmaster.ticketplusserver.model;

import jakarta.persistence.*;
/**
 * Clase Usuari que conforma l'entitat usuari per fer LOGIN, de moment
 * nomes conforma les dades nom del usuari i contrasenya, pero mes 
 * endavant sera suceptible de canvi.
 * @author David
 */
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String password;
    private String role;
    

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
}