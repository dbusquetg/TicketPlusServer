package com.ticketingmaster.ticketplusserver.controller;

import com.ticketingmaster.ticketplusserver.dto.TicketRequest;
import com.ticketingmaster.ticketplusserver.dto.TicketResponse;
import com.ticketingmaster.ticketplusserver.dto.ChangeStatusRequest;
import com.ticketingmaster.ticketplusserver.dto.ChangePriorityRequest;
import com.ticketingmaster.ticketplusserver.dto.AssignAgentRequest;
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
 *   POST  /api/tickets              → Crear ticket         (USER, ADMIN)
 *   GET   /api/tickets              → Listar tickets       (USER ve los suyos, ADMIN ve todos)
 *   GET   /api/tickets/{id}         → Detalle de ticket    (USER solo los suyos, ADMIN cualquiera)
 *   PUT   /api/tickets/{id}/assign  → Asignar agente       (solo ADMIN)
 *   PATCH /api/tickets/{id}/status  → Cambiar estado       (solo ADMIN)
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
     * ADMIN puede ver cualquier ticket.
     * USER solo puede ver los suyos — devuelve 404 si intenta ver uno ajeno.
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
     * El ADMIN autenticado se asigna a sí mismo el ticket.
     * No recibe body — el agente se toma directamente del JWT.
     * El status cambia automáticamente a "In Progress".
     */
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> asignarAgente(@PathVariable Long id,
                                                    Authentication auth) {
        try {
            return ResponseEntity.ok(servTicket.asignarAgente(id, auth.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * El ADMIN asigna el ticket a otro agente indicado en el body.
     * El status cambia automáticamente a "In Progress".
     *
     * Recibe: { "agentUsername": "erik" }
     */
    @PatchMapping("/{id}/agent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> asignarOtroAgente(@PathVariable Long id,
                                                            @RequestBody AssignAgentRequest request) {
        try {
            return ResponseEntity.ok(
                    servTicket.asignarOtroAgente(id, request.getAgentUsername())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
 
    /**
     * Cambia el estado de un ticket.
     * Acceso exclusivo para ADMIN.
     *
     * Recibe el nuevo estado como texto legible en el body:
     *   { "status": "In Progress" }
     *
     * Valores válidos: "Opened", "Pending", "In Progress",
     *                  "Resolved", "Solved", "Closed"
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> cambiarEstado(@PathVariable Long id,
                                                        @RequestBody ChangeStatusRequest request) {
        try {
            return ResponseEntity.ok(servTicket.cambiarEstado(id, request.getStatus()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Cambia la prioridad de un ticket.
     * ADMIN puede cambiar la prioridad de cualquier ticket.
     * USER solo puede cambiar la prioridad de sus propios tickets
     * — devuelve 404 si intenta modificar uno ajeno.
     *
     * Valores válidos: "LOW", "MEDIUM", "HIGH", "CRITICAL"
     */
    @PatchMapping("/{id}/priority")
    public ResponseEntity<TicketResponse> cambiarPrioridad(@PathVariable Long id,
                                                           @RequestBody ChangePriorityRequest request,
                                                           Authentication auth) {
        try {
            return ResponseEntity.ok(
                    servTicket.cambiarPrioridad(id, request.getPriority(),
                                                auth.getName(), esAdmin(auth))
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Cierra un ticket cambiando su estado a "Closed".
     * ADMIN puede cerrar cualquier ticket.
     * USER solo puede cerrar sus propios tickets
     * — devuelve 403 Forbidden si intenta cerrar uno ajeno.
     */
    @PatchMapping("/{id}/close")
    public ResponseEntity<TicketResponse> cerrarTicket(@PathVariable Long id,
                                                       Authentication auth) {
        try {
            return ResponseEntity.ok(
                    servTicket.cerrarTicket(id, auth.getName(), esAdmin(auth))
            );
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
