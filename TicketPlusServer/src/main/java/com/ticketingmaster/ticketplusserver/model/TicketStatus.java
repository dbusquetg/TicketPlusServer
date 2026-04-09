package com.ticketingmaster.ticketplusserver.model;

/**
 * Estados posibles de un ticket de soporte.
 *
 * UNASSIGNED  — Ticket creado, sin agente asignado.        → "Opened"
 * PENDING     — En espera de respuesta del cliente.        → "Pending"
 * IN_PROGRESS — Asignado a un agente, en curso.            → "In Progress"
 * RESOLVED    — Resuelto internamente.                     → "Resolved"
 * SOLVED      — Confirmado como solucionado por el cliente.→ "Solved"
 * CLOSED      — Cerrado definitivamente.                   → "Closed"
 */
public enum TicketStatus {
    UNASSIGNED,
    PENDING,
    IN_PROGRESS,
    RESOLVED,
    SOLVED,
    CLOSED
}
