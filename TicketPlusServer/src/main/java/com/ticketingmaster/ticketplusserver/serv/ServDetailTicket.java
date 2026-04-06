package com.ticketingmaster.ticketplusserver.serv;

import com.ticketingmaster.ticketplusserver.dto.DetailTicketRequest;
import com.ticketingmaster.ticketplusserver.dto.DetailTicketResponse;
import com.ticketingmaster.ticketplusserver.model.DetailTicket;
import com.ticketingmaster.ticketplusserver.model.DetailType;
import com.ticketingmaster.ticketplusserver.model.Role;
import com.ticketingmaster.ticketplusserver.model.Ticket;
import com.ticketingmaster.ticketplusserver.model.User;
import com.ticketingmaster.ticketplusserver.repo.DetailTicketRepo;
import com.ticketingmaster.ticketplusserver.repo.TicketRepo;
import com.ticketingmaster.ticketplusserver.repo.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión del hilo de conversación de tickets.
 *
 * El tipo de entrada (T o R) se asigna automáticamente según el rol
 * del usuario autenticado: USER → T, ADMIN → R.
 * Así el cliente nunca puede falsificar el tipo de su mensaje.
 */
@Service
public class ServDetailTicket {

    private final DetailTicketRepo detailRepo;
    private final TicketRepo       ticketRepo;
    private final UserRepo         userRepo;

    public ServDetailTicket(DetailTicketRepo detailRepo,
                            TicketRepo ticketRepo,
                            UserRepo userRepo) {
        this.detailRepo = detailRepo;
        this.ticketRepo = ticketRepo;
        this.userRepo   = userRepo;
    }


    /**
     * Añade una nueva entrada al hilo de conversación de un ticket.
     * El tipo (T/R) se determina automáticamente por el rol del usuario:
     *   - USER  → T (pregunta/mensaje del cliente)
     *   - ADMIN → R (respuesta del gestor)
     *
     * @param ticketId ID del ticket al que pertenece la entrada.
     * @param request  contenido de la entrada.
     * @param username username del usuario autenticado (extraído del JWT).
     * @return DetailTicketResponse con los datos de la entrada creada.
     */
    @Transactional
    public DetailTicketResponse añadir(Long ticketId,
                                       DetailTicketRequest request,
                                       String username) {

        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + ticketId));

        User author = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

        DetailType type = author.getRole() == Role.ADMIN ? DetailType.R : DetailType.T;

        DetailTicket detail = new DetailTicket(ticket, type, request.getContentDetail(), author);

        return DetailTicketResponse.from(detailRepo.save(detail));
    }

    // ─── Consultar hilo ───────────────────────────────────────────────────

    /**
     * Devuelve el hilo completo de conversación de un ticket,
     * ordenado cronológicamente.
     *
     * @param ticketId ID del ticket.
     * @return lista de entradas ordenadas por fecha ascendente.
     */
    @Transactional(readOnly = true)
    public List<DetailTicketResponse> obtenerHilo(Long ticketId) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + ticketId));

        return detailRepo.findByTicketOrderByCreationDateAsc(ticket).stream()
                .map(DetailTicketResponse::from)
                .collect(Collectors.toList());
    }
}
