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
     *
     * @param request  datos del ticket: title, description, priority,
     *                 typology (opcional), subTypology (opcional).
     * @param username username del usuario autenticado extraído del JWT.
     * @return TicketResponse con todos los datos del ticket creado.
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
     *
     * @return lista completa de tickets.
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
     *
     * @param username username del cliente autenticado.
     * @return lista de tickets del cliente.
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
     *
     * @param username username del agente.
     * @return lista de tickets asignados al agente.
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> obtenerPorAgente(String username) {
        User agent = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        return ticketRepo.findByAgent(agent).stream()
                .map(TicketResponse::from)
                .collect(Collectors.toList());
    }
 
    /**
     * Devuelve un ticket por su ID.
     *
     * @param id ID del ticket.
     * @return TicketResponse con los datos del ticket.
     */
    @Transactional(readOnly = true)
    public TicketResponse obtenerPorId(Long id) {
        Ticket ticket = ticketRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + id));
        return TicketResponse.from(ticket);
    }
 
    // ─── Asignar agente ───────────────────────────────────────────────────
 
    /**
     * Asigna un agente a un ticket y lo pone en estado IN_PROGRESS.
     * Solo puede ejecutarlo un ADMIN.
     *
     * @param ticketId      ID del ticket a asignar.
     * @param agentUsername username del agente a asignar.
     * @return TicketResponse actualizado.
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
     *
     * @param ticketId ID del ticket.
     * @param status   nuevo estado (UNASSIGNED, IN_PROGRESS, RESOLVED).
     * @return TicketResponse actualizado.
     */
    @Transactional
    public TicketResponse cambiarEstado(Long ticketId, TicketStatus status) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + ticketId));
 
        ticket.setStatus(status);
        return TicketResponse.from(ticketRepo.save(ticket));
    }
}