/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ticketingmaster.ticketplusserver.controller;
import com.ticketingmaster.ticketplusserver.serv.ServAuth;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthControlador {

    private final ServAuth authServei;

    public AuthControlador(ServAuth authService) {
        this.authServei = authService;
    }

    @PostMapping("/login")
    public LoginResposta login(@RequestBody LoginPeticio request) {

        boolean valid = authServei.login(
                request.getNomusuari(),
                request.getContrasenya()
        );

        if (valid) {
            return new LoginResposta(true, "Login correcto");
        } else {
            return new LoginResposta(false, "Credenciales incorrectas");
        }
    }
}