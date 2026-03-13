/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ticketingmaster.ticketplusserver.model;

import jakarta.persistence.*;

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