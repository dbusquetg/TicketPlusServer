package com.ticketingmaster.ticketplusserver.controller;

import com.ticketingmaster.ticketplusserver.dto.AgentResponse;
import com.ticketingmaster.ticketplusserver.dto.CreateUserRequest;
import com.ticketingmaster.ticketplusserver.dto.UserResponse;
import com.ticketingmaster.ticketplusserver.serv.ServUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de usuarios.
 * Endpoints:
 *   GET    /api/users            → Listar usuarios activos    (solo ADMIN)
 *   POST   /api/users            → Crear o reactivar usuario  (solo ADMIN)
 *   DELETE /api/users/{username} → Desactivar usuario         (solo ADMIN)
 *   GET    /api/users/agents     → Listar agentes activos     (solo ADMIN)
 * @author David Busquet
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final ServUser servUser;
    /**
     * Constructor, recibe el servicio de usuario com servUser
     * @param servUser ServUser
     */
    public UserController(ServUser servUser) {
        this.servUser = servUser;
    }

    /**
     * Lista todos los usuarios activos del sistema (ADMIN + USER).
     * Ordenados por username ascendente.
     * Nunca expone passwordHash.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> listarTodos() {
        return ResponseEntity.ok(servUser.listarTodos());
    }

    /**
     * Crea un nuevo usuario o reactiva uno desactivado previamente.
     * Solo ADMIN puede crear usuarios.
     *
     * Body: { "username": "...", "password": "...", "role": "USER|ADMIN" }
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@RequestBody CreateUserRequest request) {
        try {
            UserResponse response = servUser.crear(request);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al crear usuario");
        }
    }

    /**
     * Desactiva un usuario (eliminación lógica — active=false).
     * No borra físicamente de la BD.
     *
     * Restricciones:
     * - No puedes eliminarte a ti mismo.
     * - No puedes eliminar el último administrador activo.
     *
     * Devuelve 204 No Content si se desactiva correctamente.
     * Devuelve 400 si se viola alguna restricción.
     * Devuelve 404 si el usuario no existe.
     */
    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminar(@PathVariable String username,
                                      Authentication auth) {
        try {
            servUser.eliminar(username, auth.getName());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lista todos los agentes (ADMIN) activos ordenados por username.
     * Usado por el cliente para cargar el combo de agentes en TicketDetailPanel.
     * Nunca expone passwordHash.
     */
    @GetMapping("/agents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AgentResponse>> listarAgentes() {
        return ResponseEntity.ok(servUser.listarAgentes());
    }
}