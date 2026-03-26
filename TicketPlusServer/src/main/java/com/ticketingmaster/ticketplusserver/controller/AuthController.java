package com.ticketingmaster.ticketplusserver.controller;
import com.ticketingmaster.ticketplusserver.dto.LoginRequest;
import com.ticketingmaster.ticketplusserver.dto.LoginResponse;
import com.ticketingmaster.ticketplusserver.serv.ServAuth;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import org.springframework.web.bind.annotation.*;
/**
 * Classe AuthControlador controla las validaciones y devuelve las respuestas del login si son correctas.
 * o incorrectas.
 * @author David Busquet
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ServAuth authService;
    /**
     * Constructor recibe un AuthService como servicio de autenticación.
     * @param authService Servicio de autenticación.
     */
    public AuthController(ServAuth authService) {
        this.authService = authService;
    }
    /**
     * Función que recibe una petición en forma de LoginRequest y devuelve si se valida o no
     * según la gestión de la autenticación del servicio.
     * @param request en forma de login petición.
     * @return ResponseEntity Entidad de respuesta.
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
     * Función que revisa el Header de la peticion para leer el token y añadirlo al blacklist
     * @param bearerToken Token a revisar del portador.
     * @return ResponseEntity. Entidad de respuesta.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearerToken) {

        authService.logout(bearerToken);
        return ResponseEntity.noContent().build(); 
        
    }
    
}