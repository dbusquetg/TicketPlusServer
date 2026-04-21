package com.ticketingmaster.ticketplusserver.serv;

import com.ticketingmaster.ticketplusserver.dto.CommentResponse;
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
 
    //  Añadir entrada al hilo (uso interno) 
 
    /**
     * Añade una nueva entrada al hilo de conversación de un ticket.
     * El tipo (T/R) se determina automáticamente por el rol del usuario.
     * @param ticketId ID del ticket buscado en la BD.
     * @param request objeto DetailTicketRequest como solicitud.
     * @param username Nombre del usuario.
     * @return Devuelve una respuesta en forma de DetailTicketResponse,
     */
    @Transactional
    public DetailTicketResponse añadir(Long ticketId,
                                       DetailTicketRequest request,
                                       String username) {
        DetailTicket detail = crearDetalle(ticketId, request.getContentDetail(), username);
        return DetailTicketResponse.from(detailRepo.saveAndFlush(detail)); // ← saveAndFlush
    }
 
    //  Añadir comentario (endpoint /comments) 
 
    /**
     * Añade un comentario al ticket y devuelve el formato CommentResponse.
     * Reutiliza la misma entidad DetailTicket internamente.
     * El author se deduce del JWT, el cliente solo envía el content.
     * @param ticketId ID del ticket buscado en la BD.
     * @param content contenido del comentario.
     * @param username Nombre del usuario.
     * @return Devuelve una respuesta en forma de CommentResponse,
     */
    @Transactional
    public CommentResponse añadirComentario(Long ticketId, String content, String username) {
        DetailTicket detail = crearDetalle(ticketId, content, username);
        return CommentResponse.from(detailRepo.saveAndFlush(detail)); // ← saveAndFlush
    }
 
    //  Obtener comentarios (endpoint /comments GET) 
 
    /**
     * Devuelve todos los comentarios de un ticket ordenados por fecha
     * ascendente, en formato CommentResponse.
     *
     * El primer comentario es siempre el del usuario que abrió el ticket,
     * ya que los comentarios se ordenan por creationDate ascendente y el
     * primero se crea en el momento de abrir el ticket.
     *
     * @param ticketId ID del ticket.
     * @return lista de comentarios ordenados cronológicamente.
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> obtenerComentarios(Long ticketId) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + ticketId));
 
        return detailRepo.findByTicketOrderByCreationDateAsc(ticket).stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }
 
    //  Consultar hilo 
 
    /**
     * Devuelve el hilo completo de conversación de un ticket,
     * ordenado cronológicamente en formato DetailTicketResponse.
     * @param ticketId ID del ticket.
     * @return una lista de respuestas en forma DetailTicketResponse
     */
    @Transactional(readOnly = true)
    public List<DetailTicketResponse> obtenerHilo(Long ticketId) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + ticketId));
 
        return detailRepo.findByTicketOrderByCreationDateAsc(ticket).stream()
                .map(DetailTicketResponse::from)
                .collect(Collectors.toList());
    }
 
    //  Helper privado 
 
    /**
     * Lógica común de creación de un DetailTicket.
     * Resuelve el ticket, el usuario y asigna el tipo T/R según el rol.
     * @param ticketId ID del ticket buscado en la BD.
     * @param content contenido del detalle.
     * @param username Nombre del usuario.
     * @return Detalle del ticket en forma de DetailTicket.,
     */
    private DetailTicket crearDetalle(Long ticketId, String content, String username) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + ticketId));
 
        User author = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
 
        DetailType type = author.getRole() == Role.ADMIN ? DetailType.R : DetailType.T;
 
        return new DetailTicket(ticket, type, content, author);
    }
}