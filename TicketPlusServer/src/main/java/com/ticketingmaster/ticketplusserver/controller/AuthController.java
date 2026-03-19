package com.ticketingmaster.ticketplusserver.controller;
import com.ticketingmaster.ticketplusserver.model.User;
import com.ticketingmaster.ticketplusserver.serv.ServAuth;
import java.util.Optional;
import java.util.Random;

import org.springframework.web.bind.annotation.*;
/**
 * Classe AuthControlador classe que controla les validacions y retorna les respostes de login si son correctes
 * o incorrectes.
 * @author David
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final ServAuth authService;

    public AuthController(ServAuth authService) {
        this.authService = authService;
    }
    /**
     * Funció que rep una petició en forma de LoginPetició i retorna si  es valida o no
     * segons la gestió de l'autenticació del servei.
     * @param request en forma de login petició.
     * @return LoginResposta.
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        Optional<User> usr = authService.login(
                request.getName(),
                request.getPassword()
        );

        Random random = new Random();
        int random_number = random.nextInt(9999);
        String id_session = String.valueOf(random_number);        
        
        if (!usr.isEmpty()) {
            return new LoginResponse(id_session, usr.get().getRole());
        } else {
            return new LoginResponse("0", "none");
        }
    }
    
    /**
     * Funció que rep una petició en forma de LoginPetició i retorna si  es valida o no
     * segons la gestió de l'autenticació del servei.
     * @param request en forma de login petició.
     * @return LoginResposta.
     */
    @PostMapping("/logout")
    public LogoutResponse logout(@RequestBody LogoutRequest request) {

        boolean valid = authService.logout(request.getIdSession());

        if (valid) {
            return new LogoutResponse("Logout exitoso");
        } else {
            return new LogoutResponse( "Error en el logout");
        }
    }
    
}