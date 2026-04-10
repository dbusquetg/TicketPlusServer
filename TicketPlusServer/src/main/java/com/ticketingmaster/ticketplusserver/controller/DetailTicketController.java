package com.ticketingmaster.ticketplusserver.controller;

import com.ticketingmaster.ticketplusserver.dto.CommentResponse;
import com.ticketingmaster.ticketplusserver.dto.DetailTicketRequest;
import com.ticketingmaster.ticketplusserver.dto.DetailTicketResponse;
import com.ticketingmaster.ticketplusserver.serv.ServDetailTicket;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
/**
 * Controlador REST para el hilo de conversación y comentarios de un ticket.
 *
 * Endpoints:
 *   POST /api/tickets/{id}/details   → Añadir entrada al hilo  (USER, ADMIN)
 *   GET  /api/tickets/{id}/details   → Ver hilo completo       (USER, ADMIN)
 *   POST /api/tickets/{id}/comments  → Añadir comentario       (USER, ADMIN)
 *   GET  /api/tickets/{id}/comments  → Obtener comentarios     (USER, ADMIN)
 *
 * Los cuatro endpoints usan la misma entidad DetailTicket internamente.
 * El tipo T/R se asigna automáticamente según el rol del usuario autenticado.
 */
@RestController
@RequestMapping("/api/tickets/{ticketId}")
public class DetailTicketController {
 
    private final ServDetailTicket servDetailTicket;
 
    public DetailTicketController(ServDetailTicket servDetailTicket) {
        this.servDetailTicket = servDetailTicket;
    }
 
    /**
     * Añade una nueva entrada al hilo del ticket.
     * USER  → tipo T (pregunta/mensaje del cliente).
     * ADMIN → tipo R (respuesta del gestor).
     */
    @PostMapping("/details")
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
     * ordenado cronológicamente.
     */
    @GetMapping("/details")
    public ResponseEntity<List<DetailTicketResponse>> obtenerHilo(
            @PathVariable Long ticketId) {
        try {
            return ResponseEntity.ok(servDetailTicket.obtenerHilo(ticketId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
 
    /**
     * Añade un comentario al ticket.
     * El author se deduce del JWT, el cliente solo envía el content.
     *
     * Recibe:  { "content": "Puedes probar ahora Maria?" }
     * Devuelve 201 Created con ticketRef, ticketTitle, author, content y createdAt.
     */
    @PostMapping("/comments")
    public ResponseEntity<CommentResponse> añadirComentario(
            @PathVariable Long ticketId,
            @RequestBody DetailTicketRequest request,
            Authentication auth) {
        try {
            CommentResponse response = servDetailTicket.añadirComentario(
                    ticketId, request.getContentDetail(), auth.getName()
            );
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
 
    /**
     * Devuelve todos los comentarios del ticket ordenados por fecha ascendente.
     * El primer comentario es siempre el del usuario que abrió el ticket.
     * Devuelve 404 si el ticket no existe.
     */
    @GetMapping("/comments")
    public ResponseEntity<List<CommentResponse>> obtenerComentarios(
            @PathVariable Long ticketId) {
        try {
            return ResponseEntity.ok(servDetailTicket.obtenerComentarios(ticketId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}