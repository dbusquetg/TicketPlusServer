package com.ticketingmaster.ticketplusserver.controller;

import com.ticketingmaster.ticketplusserver.dto.DetailTicketRequest;
import com.ticketingmaster.ticketplusserver.dto.DetailTicketResponse;
import com.ticketingmaster.ticketplusserver.serv.ServDetailTicket;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el hilo de conversación de un ticket.
 *
 * Endpoints:
 *   POST /api/tickets/{id}/details      → Añadir entrada al hilo (USER, ADMIN)
 *   GET  /api/tickets/{id}/details      → Ver hilo completo      (USER, ADMIN)
 *
 * El acceso está protegido por JWT (anyRequest → authenticated en SecurityConfig).
 * El tipo de entrada (T/R) se asigna automáticamente en el servicio
 * según el rol del usuario autenticado.
 */
@RestController
@RequestMapping("/api/tickets/{ticketId}/details")
public class DetailTicketController {

    private final ServDetailTicket servDetailTicket;

    public DetailTicketController(ServDetailTicket servDetailTicket) {
        this.servDetailTicket = servDetailTicket;
    }

    /**
     * Añade una nueva entrada al hilo del ticket.
     * USER  → tipo T (pregunta/mensaje del cliente).
     * ADMIN → tipo R (respuesta del gestor).
     *
     * @param ticketId ID del ticket.
     * @param request  contenido de la entrada.
     * @param auth     usuario autenticado extraído del JWT.
     */
    @PostMapping
    public ResponseEntity<DetailTicketResponse> añadir(
            @PathVariable Long ticketId,
            @RequestBody DetailTicketRequest request,
            Authentication auth) {
        try {
            DetailTicketResponse response =
                    servDetailTicket.añadir(ticketId, request, auth.getName());
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Devuelve el hilo completo de conversación del ticket,
     * ordenado cronológicamente (pregunta → respuesta → ...).
     *
     * @param ticketId ID del ticket.
     */
    @GetMapping
    public ResponseEntity<List<DetailTicketResponse>> obtenerHilo(
            @PathVariable Long ticketId) {
        try {
            return ResponseEntity.ok(servDetailTicket.obtenerHilo(ticketId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
