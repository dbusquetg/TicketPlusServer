
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
public class Usuari {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomusuari;
    private String contrasenya;

    public String getNomusuari() {
        return nomusuari;
    }

    public void setNomusuari(String nomusuari) {
        this.nomusuari = nomusuari;
    }

    public String getContrasenya() {
        return contrasenya;
    }

    public void setContrasenya(String contrasenya) {
        this.contrasenya = contrasenya;
    }
    
    

}