package com.ticketingmaster.ticketplusserver.controller;

import com.ticketingmaster.ticketplusserver.dto.AgentResponse;
import com.ticketingmaster.ticketplusserver.serv.ServUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
/**
 * Controlador REST para la gestión de usuarios.
 *
 * Endpoints:
 *   GET /api/users/agents → Listar agentes (solo ADMIN)
 * @author David.Busquet
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
 
    private final ServUser servUser;
 
    public UserController(ServUser servUser) {
        this.servUser = servUser;
    }
 
    /**
     * Devuelve todos los usuarios con rol ADMIN.
     * Usado por el cliente para cargar dinámicamente
     * el combo de agentes en TicketDetailPanel.
     *
     * Devuelve 403 si el usuario autenticado no tiene rol ADMIN.
     * Nunca expone passwordHash ni active.
     */
    @GetMapping("/agents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AgentResponse>> listarAgentes() {
        return ResponseEntity.ok(servUser.listarAgentes());
    }
}