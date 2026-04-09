/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ticketingmaster.ticketplusserver.dto;

/**
 * DTO para cambiar la prioridad de un ticket.
 *
 * Valores válidos: "LOW", "MEDIUM", "HIGH", "CRITICAL"
 *
 * Ejemplo de JSON recibido:
 * {
 *   "priority": "LOW"
 * }
 * @author David.Busquet
 */
public class ChangePriorityRequest {
 
    private String priority;
 
    public ChangePriorityRequest() {}
 
    public String getPriority()              { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
 
