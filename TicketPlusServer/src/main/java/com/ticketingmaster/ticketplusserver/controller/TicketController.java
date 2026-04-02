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
 *   POST   /api/tickets              → Crear ticket        (USER, ADMIN)
 *   GET    /api/tickets              → Listar todos        (solo ADMIN)
 *   GET    /api/tickets/mine         → Mis tickets         (USER autenticado)
 *   GET    /api/tickets/assigned     → Tickets asignados   (ADMIN/agente)
 *   GET    /api/tickets/{id}         → Detalle de ticket   (USER, ADMIN)
 *   PUT    /api/tickets/{id}/assign  → Asignar agente      (solo ADMIN)
 *   PUT    /api/tickets/{id}/status  → Cambiar estado      (solo ADMIN)
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final ServTicket servTicket;

    public TicketController(ServTicket servTicket) {
        this.servTicket = servTicket;
    }

    /**
     * Crea un nuevo ticket. El creador se obtiene del JWT,
     * no del body de la petición.
     */
    @PostMapping
    public ResponseEntity<TicketResponse> crear(@RequestBody TicketRequest request,
                                                 Authentication auth) {
        try {
            TicketResponse response = servTicket.crear(request, auth.getName());
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Lista todos los tickets del sistema.
     * Acceso exclusivo para administradores.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketResponse>> obtenerTodos() {
        return ResponseEntity.ok(servTicket.obtenerTodos());
    }

    /**
     * Devuelve los tickets creados por el usuario autenticado.
     */
    @GetMapping("/mine")
    public ResponseEntity<List<TicketResponse>> misTikets(Authentication auth) {
        return ResponseEntity.ok(servTicket.obtenerPorCliente(auth.getName()));
    }

    /**
     * Devuelve los tickets asignados al agente autenticado.
     * Acceso exclusivo para administradores/agentes.
     */
    @GetMapping("/assigned")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketResponse>> ticketsAsignados(Authentication auth) {
        return ResponseEntity.ok(servTicket.obtenerPorAgente(auth.getName()));
    }

    /**
     * Devuelve el detalle de un ticket por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(servTicket.obtenerPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Asigna un agente a un ticket y lo pone en estado IN_PROGRESS.
     * Acceso exclusivo para administradores.
     *
     * @param id            ID del ticket.
     * @param agentUsername username del agente a asignar (query param).
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
     * Acceso exclusivo para administradores.
     *
     * @param id     ID del ticket.
     * @param status nuevo estado (UNASSIGNED, IN_PROGRESS, RESOLVED).
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
