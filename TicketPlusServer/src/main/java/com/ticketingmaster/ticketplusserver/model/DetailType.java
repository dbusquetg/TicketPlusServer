package com.ticketingmaster.ticketplusserver.model;

/**
 * Tipo de entrada en el hilo de conversación de un ticket.
 *
 * T — Pregunta o mensaje del cliente (de "Ticket" o "Question").
 * R — Respuesta del gestor/agente.
 * @author David Busquet Gimeno
 */
public enum DetailType {
    T,
    R
}
