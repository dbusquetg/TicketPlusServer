package com.ticketingmaster.ticketplusserver.controller;
import com.ticketingmaster.ticketplusserver.dto.LoginRequest;
import com.ticketingmaster.ticketplusserver.dto.LoginResponse;
import com.ticketingmaster.ticketplusserver.serv.ServAuth;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import org.springframework.web.bind.annotation.*;
/**
 * Classe AuthControlador classe que controla les validacions y retorna les respostes de login si son correctes
 * o incorrectes.
 * @author David
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ServAuth authService;

    public AuthController(ServAuth authService) {
        this.authService = authService;
    }
    /**
     * Funció que rep una petició en forma de LoginRequest i retorna si  es valida o no
     * segons la gestió de l'autenticació del servei.
     * @param request en forma de login petició.
     * @return ResponseEntity
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        try{
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        }catch(DisabledException e){
            return ResponseEntity.status(403).body("Usuario deshabilitado");
        }catch(BadCredentialsException e){
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }catch(Exception e){
            return ResponseEntity.status(500).body("Error del servidor");
        }
        
        
    }
    
    /**
     * Funció que revisa el Header de la peticio per llegir el token y afegirlo al blacklist
     * @param bearerToken 
     * @return ResponseEntity.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearerToken) {

        authService.logout(bearerToken);
        return ResponseEntity.noContent().build(); 
        
    }
    
}