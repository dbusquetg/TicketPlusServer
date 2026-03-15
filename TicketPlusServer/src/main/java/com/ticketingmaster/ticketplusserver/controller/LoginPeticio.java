
package com.ticketingmaster.ticketplusserver.controller;

/**
 * Clase que conforma la petició de login, formada per un nom d'usuari i una contrasenya.
 * @author David
 */
public class LoginPeticio {

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