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
     * Crea un nuevo ticket. El estado inicial es siempre UNASSIGNED.
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
     * Devuelve todos los tickets creados por el usuario autenticado.
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
     * ADMIN puede ver cualquier ticket.
     * USER solo puede ver sus propios tickets — si intenta ver uno ajeno
     * se lanza RuntimeException para devolver 404 (no 403).
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
     * Cambia el estado de un ticket a partir del texto legible enviado
     * por el cliente. Solo puede ejecutarlo un ADMIN.
     *
     * Valores válidos recibidos del cliente:
     *   "Opened", "Pending", "In Progress", "Resolved", "Solved", "Closed"
     *
     * @param ticketId  ID del ticket.
     * @param statusStr estado en texto legible enviado por el cliente.
     * @return TicketResponse actualizado.
     * @throws IllegalArgumentException si el valor de status no es válido.
     */
    @Transactional
    public TicketResponse cambiarEstado(Long ticketId, String statusStr) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + ticketId));
 
        ticket.setStatus(parseStatus(statusStr));
        return TicketResponse.from(ticketRepo.save(ticket));
    }
 
    /**
     * Traduce el texto legible del cliente al enum interno TicketStatus.
     *
     * @param statusStr texto enviado por el cliente.
     * @return TicketStatus correspondiente.
     * @throws IllegalArgumentException si el valor no es reconocido.
     */
    private TicketStatus parseStatus(String statusStr) {
        return switch (statusStr) {
            case "Opened"      -> TicketStatus.UNASSIGNED;
            case "Pending"     -> TicketStatus.PENDING;
            case "In Progress" -> TicketStatus.IN_PROGRESS;
            case "Resolved"    -> TicketStatus.RESOLVED;
            case "Solved"      -> TicketStatus.SOLVED;
            case "Closed"      -> TicketStatus.CLOSED;
            default -> throw new IllegalArgumentException(
                    "Estado no válido: '" + statusStr + "'. " +
                    "Valores aceptados: Opened, Pending, In Progress, Resolved, Solved, Closed"
            );
        };
    }
}