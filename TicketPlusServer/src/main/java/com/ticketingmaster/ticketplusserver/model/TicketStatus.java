package com.ticketingmaster.ticketplusserver.model;

/**
 * Estados posibles de un ticket de soporte.
 *
 * UNASSIGNED  — Ticket creado, sin agente asignado.
 * IN_PROGRESS — Ticket asignado a un agente, en curso.
 * RESOLVED    — Ticket resuelto y cerrado.
 */
public enum TicketStatus {
    UNASSIGNED,
    PENDING,
    IN_PROGRESS,
    RESOLVED
}
