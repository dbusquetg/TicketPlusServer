package com.ticketingmaster.ticketplusserver.model;

/**
 * Niveles de prioridad de un ticket.
 *
 * LOW      — Baja prioridad, sin urgencia.
 * MEDIUM   — Prioridad media, atención normal.
 * HIGH     — Alta prioridad, atención preferente.
 * CRITICAL — Crítico, requiere atención inmediata.
 * @author David Busquet Gimeno
 */
public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
 