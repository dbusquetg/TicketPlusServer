package com.ticketingmaster.ticketplusserver.serv;

import com.ticketingmaster.ticketplusserver.dto.TicketRequest;
import com.ticketingmaster.ticketplusserver.dto.TicketResponse;
import com.ticketingmaster.ticketplusserver.model.Ticket;
import com.ticketingmaster.ticketplusserver.model.TicketStatus;
import com.ticketingmaster.ticketplusserver.model.User;
import com.ticketingmaster.ticketplusserver.repo.TicketRepo;
import com.ticketingmaster.ticketplusserver.repo.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de tickets.
 * El usuario autenticado se extrae siempre del JWT via SecurityContext,
 * nunca del body de la petición.
 */
@Service
public class ServTicket {
 
    private final TicketRepo ticketRepo;
    private final UserRepo   userRepo;
 
    public ServTicket(TicketRepo ticketRepo, UserRepo userRepo) {
        this.ticketRepo = ticketRepo;
        this.userRepo   = userRepo;
    }
 
    // ─── Crear ────────────────────────────────────────────────────────────
 
    /**
     * Crea un nuevo ticket a partir del JSON recibido del cliente.
     * El creador se resuelve a partir del username extraído del JWT.
     */
    @Transactional
    public TicketResponse crear(TicketRequest request, String username) {
        User creator = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
 
        Ticket ticket = new Ticket(
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                request.getTypology(),
                request.getSubTypology(),
                creator
        );
 
        return TicketResponse.from(ticketRepo.save(ticket));
    }
 
    // ─── Listar ───────────────────────────────────────────────────────────
 
    /**
     * Devuelve todos los tickets del sistema.
     * Llamado cuando el usuario autenticado tiene rol ADMIN.
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> obtenerTodos() {
        return ticketRepo.findAll().stream()
                .map(TicketResponse::from)
                .collect(Collectors.toList());
    }
 
    /**
     * Devuelve solo los tickets creados por el usuario autenticado.
     * Llamado cuando el usuario autenticado tiene rol USER.
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> obtenerPorCliente(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        return ticketRepo.findByCreatedBy(user).stream()
                .map(TicketResponse::from)
                .collect(Collectors.toList());
    }
 
    /**
     * Devuelve los tickets asignados a un agente concreto.
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> obtenerPorAgente(String username) {
        User agent = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        return ticketRepo.findByAgent(agent).stream()
                .map(TicketResponse::from)
                .collect(Collectors.toList());
    }
 
    // ─── Detalle ──────────────────────────────────────────────────────────
 
    /**
     * Devuelve el detalle de un ticket por su ID.
     *
     * Si el usuario es ADMIN puede ver cualquier ticket.
     * Si el usuario es USER solo puede ver sus propios tickets —
     * si intenta ver uno ajeno se lanza TicketNotFoundException
     * para que el controlador devuelva 404 (no 403, para no revelar
     * que el ticket existe).
     *
     * @param id       ID del ticket.
     * @param username username del usuario autenticado extraído del JWT.
     * @param esAdmin  true si el usuario tiene rol ADMIN.
     * @return TicketResponse con los datos del ticket.
     * @throws RuntimeException si el ticket no existe o el USER no es el propietario.
     */
    @Transactional(readOnly = true)
    public TicketResponse obtenerPorId(Long id, String username, boolean esAdmin) {
        Ticket ticket = ticketRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + id));
 
        if (!esAdmin && !ticket.getCreatedBy().getUsername().equals(username)) {
            throw new RuntimeException("Ticket no encontrado: " + id);
        }
 
        return TicketResponse.from(ticket);
    }
 
    // ─── Asignar agente ───────────────────────────────────────────────────
 
    /**
     * Asigna un agente a un ticket y lo pone en estado IN_PROGRESS.
     * Solo puede ejecutarlo un ADMIN.
     */
    @Transactional
    public TicketResponse asignarAgente(Long ticketId, String agentUsername) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + ticketId));
        User agent = userRepo.findByUsername(agentUsername)
                .orElseThrow(() -> new RuntimeException("Agente no encontrado: " + agentUsername));
 
        ticket.setAgent(agent);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        return TicketResponse.from(ticketRepo.save(ticket));
    }
 
    // ─── Cambiar estado ───────────────────────────────────────────────────
 
    /**
     * Cambia el estado de un ticket.
     * Solo puede ejecutarlo un ADMIN.
     */
    @Transactional
    public TicketResponse cambiarEstado(Long ticketId, TicketStatus status) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + ticketId));
 
        ticket.setStatus(status);
        return TicketResponse.from(ticketRepo.save(ticket));
    }
}