package com.ticketingmaster.ticketplusserver.controller;

import com.ticketingmaster.ticketplusserver.dto.TicketRequest;
import com.ticketingmaster.ticketplusserver.dto.TicketResponse;
import com.ticketingmaster.ticketplusserver.model.TicketStatus;
import com.ticketingmaster.ticketplusserver.serv.ServTicket;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de tickets.
 *
 * Endpoints:
 *   POST /api/tickets              → Crear ticket         (USER, ADMIN)
 *   GET  /api/tickets              → Listar tickets       (USER ve los suyos, ADMIN ve todos)
 *   GET  /api/tickets/{id}         → Detalle de ticket    (USER solo los suyos, ADMIN cualquiera)
 *   PUT  /api/tickets/{id}/assign  → Asignar agente       (solo ADMIN)
 *   PUT  /api/tickets/{id}/status  → Cambiar estado       (solo ADMIN)
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {
 
    private final ServTicket servTicket;
 
    public TicketController(ServTicket servTicket) {
        this.servTicket = servTicket;
    }
 
    // ─── Helper ───────────────────────────────────────────────────────────
 
    private boolean esAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
 
    // ─── Endpoints ────────────────────────────────────────────────────────
 
    /**
     * Crea un nuevo ticket.
     * El creador se obtiene del JWT, no del body.
     */
    @PostMapping
    public ResponseEntity<TicketResponse> crear(@RequestBody TicketRequest request,
                                                Authentication auth) {
        try {
            return ResponseEntity.status(201).body(servTicket.crear(request, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
 
    /**
     * Lista tickets según el rol del usuario autenticado.
     * ADMIN → todos los tickets del sistema.
     * USER  → solo los tickets que él ha creado.
     */
    @GetMapping
    public ResponseEntity<List<TicketResponse>> listar(Authentication auth) {
        List<TicketResponse> tickets = esAdmin(auth)
                ? servTicket.obtenerTodos()
                : servTicket.obtenerPorCliente(auth.getName());
 
        return ResponseEntity.ok(tickets);
    }
 
    /**
     * Devuelve el detalle de un ticket por su ID.
     *
     * ADMIN → puede ver cualquier ticket.
     * USER  → solo puede ver sus propios tickets.
     *         Si intenta ver uno ajeno devuelve 404
     *         (no 403, para no revelar que el ticket existe).
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> obtenerPorId(@PathVariable Long id,
                                                       Authentication auth) {
        try {
            return ResponseEntity.ok(
                    servTicket.obtenerPorId(id, auth.getName(), esAdmin(auth))
            );
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
 
    /**
     * Asigna un agente a un ticket y lo pone en estado IN_PROGRESS.
     * Acceso exclusivo para ADMIN.
     */
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> asignarAgente(@PathVariable Long id,
                                                        @RequestParam String agentUsername) {
        try {
            return ResponseEntity.ok(servTicket.asignarAgente(id, agentUsername));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
 
    /**
     * Cambia el estado de un ticket.
     * Acceso exclusivo para ADMIN.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> cambiarEstado(@PathVariable Long id,
                                                        @RequestParam TicketStatus status) {
        try {
            return ResponseEntity.ok(servTicket.cambiarEstado(id, status));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
