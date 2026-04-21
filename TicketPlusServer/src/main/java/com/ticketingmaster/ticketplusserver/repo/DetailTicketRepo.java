package com.ticketingmaster.ticketplusserver.repo;

import com.ticketingmaster.ticketplusserver.model.DetailTicket;
import com.ticketingmaster.ticketplusserver.model.DetailType;
import com.ticketingmaster.ticketplusserver.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la entidad DetailTicket.
 * Proporciona consultas para gestionar el hilo de conversación
 * de cada ticket.
 * @author David Busquet Gimeno
 */
public interface DetailTicketRepo extends JpaRepository<DetailTicket, Long> {

    /** Todas las entradas del hilo de un ticket, en orden cronológico. */
    List<DetailTicket> findByTicketOrderByCreationDateAsc(Ticket ticket);

    /** Entradas de un ticket filtradas por tipo (T o R). */
    List<DetailTicket> findByTicketAndTypeDetail(Ticket ticket, DetailType typeDetail);

    /** Número de entradas en el hilo de un ticket. */
    long countByTicket(Ticket ticket);
}
