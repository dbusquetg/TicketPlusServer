package com.ticketingmaster.ticketplusserver.controller;
import com.ticketingmaster.ticketplusserver.serv.ServAuth;

import org.springframework.web.bind.annotation.*;
/**
 * Classe AuthControlador classe que controla les validacions y retorna les respostes de login si son correctes
 * o incorrectes.
 * @author David
 */
@RestController
@RequestMapping("/api")
public class AuthControlador {

    private final ServAuth authServei;

    public AuthControlador(ServAuth authService) {
        this.authServei = authService;
    }
    /**
     * Funció que rep una petició en forma de LoginPetició i retorna si  es valida o no
     * segons la gestió de l'autenticació del servei.
     * @param request en forma de login petició.
     * @return LoginResposta.
     */
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