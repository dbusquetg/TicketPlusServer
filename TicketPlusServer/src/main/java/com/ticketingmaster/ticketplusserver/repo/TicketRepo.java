package com.ticketingmaster.ticketplusserver.repo;

import com.ticketingmaster.ticketplusserver.model.Ticket;
import com.ticketingmaster.ticketplusserver.model.TicketStatus;
import com.ticketingmaster.ticketplusserver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la entidad Ticket.
 * Proporciona consultas derivadas para los casos de uso principales
 * del sistema de ticketing.
 */
public interface TicketRepo extends JpaRepository<Ticket, Long> {

    /** Todos los tickets creados por un cliente concreto. */
    List<Ticket> findByCreatedBy(User createdBy);

    /** Todos los tickets asignados a un agente concreto. */
    List<Ticket> findByAgent(User agent);

    /** Todos los tickets en un estado determinado. */
    List<Ticket> findByStatus(TicketStatus status);

    /** Tickets de un agente filtrados por estado. */
    List<Ticket> findByAgentAndStatus(User agent, TicketStatus status);

    /** Tickets sin agente asignado (bandeja de entrada). */
    List<Ticket> findByAgentIsNull();

    /** Tickets de un cliente filtrados por estado. */
    List<Ticket> findByCreatedByAndStatus(User createdBy, TicketStatus status);
}
