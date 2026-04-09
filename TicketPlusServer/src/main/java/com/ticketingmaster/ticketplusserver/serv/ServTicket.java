package com.ticketingmaster.ticketplusserver.serv;

import com.ticketingmaster.ticketplusserver.dto.TicketRequest;
import com.ticketingmaster.ticketplusserver.dto.TicketResponse;
import com.ticketingmaster.ticketplusserver.model.Priority;
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
    
    /**
     * Cierra un ticket cambiando su estado a CLOSED.
     * ADMIN puede cerrar cualquier ticket.
     * USER solo puede cerrar sus propios tickets —
     * si intenta cerrar uno ajeno se lanza SecurityException
     * para que el controlador devuelva 403 Forbidden.
     *
     * @param ticketId ID del ticket a cerrar.
     * @param username username del usuario autenticado extraído del JWT.
     * @param esAdmin  true si el usuario tiene rol ADMIN.
     * @return TicketResponse con status CLOSED.
     * @throws SecurityException si el USER intenta cerrar un ticket ajeno.
     * @throws RuntimeException  si el ticket no existe.
     */
    @Transactional
    public TicketResponse cerrarTicket(Long ticketId, String username, boolean esAdmin) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + ticketId));
 
        if (!esAdmin && !ticket.getCreatedBy().getUsername().equals(username)) {
            throw new SecurityException("No tienes permiso para cerrar este ticket");
        }
 
        ticket.setStatus(TicketStatus.CLOSED);
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
     * El ADMIN autenticado se asigna a sí mismo el ticket.
     * El agente se toma del JWT — no se recibe en el body.
     * El estado cambia automáticamente a IN_PROGRESS.
     *
     * @param ticketId ID del ticket a asignar.
     * @param username username del ADMIN autenticado extraído del JWT.
     * @return TicketResponse actualizado con agent y status IN_PROGRESS.
     */
    @Transactional
    public TicketResponse asignarAgente(Long ticketId, String username) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + ticketId));
 
        User agent = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
 
        ticket.setAgent(agent);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        return TicketResponse.from(ticketRepo.save(ticket));
    }
    
    /**
     * El ADMIN asigna el ticket a otro agente indicado en el body.
     * El estado cambia automáticamente a IN_PROGRESS.
     *
     * @param ticketId      ID del ticket a reasignar.
     * @param agentUsername username del agente destino recibido en el body.
     */
    @Transactional
    public TicketResponse asignarOtroAgente(Long ticketId, String agentUsername) {
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
 
    /**
     * Cambia la prioridad de un ticket.
     * ADMIN puede cambiar la prioridad de cualquier ticket.
     * USER solo puede cambiar la prioridad de sus propios tickets —
     * si intenta modificar uno ajeno se devuelve 404.
     *
     * Valores válidos: "LOW", "MEDIUM", "HIGH", "CRITICAL"
     *
     * @param ticketId    ID del ticket.
     * @param priorityStr prioridad en texto enviada por el cliente.
     * @param username    username del usuario autenticado extraído del JWT.
     * @param esAdmin     true si el usuario tiene rol ADMIN.
     * @return TicketResponse actualizado.
     * @throws IllegalArgumentException si la prioridad no es válida.
     * @throws RuntimeException         si el ticket no existe o el USER intenta modificar uno ajeno.
     */
    @Transactional
    public TicketResponse cambiarPrioridad(Long ticketId, String priorityStr,
                                           String username, boolean esAdmin) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + ticketId));
 
        if (!esAdmin && !ticket.getCreatedBy().getUsername().equals(username)) {
            throw new RuntimeException("Ticket no encontrado: " + ticketId);
        }
 
        ticket.setPriority(parsePriority(priorityStr));
        return TicketResponse.from(ticketRepo.save(ticket));
    }
 
    /**
     * Traduce el texto del cliente al enum interno Priority.
     */
    private Priority parsePriority(String priorityStr) {
        return switch (priorityStr) {
            case "LOW"      -> Priority.LOW;
            case "MEDIUM"   -> Priority.MEDIUM;
            case "HIGH"     -> Priority.HIGH;
            case "CRITICAL" -> Priority.CRITICAL;
            default -> throw new IllegalArgumentException(
                    "Prioridad no válida: '" + priorityStr + "'. " +
                    "Valores aceptados: LOW, MEDIUM, HIGH, CRITICAL"
            );
        };
    }
    
}