/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ticketingmaster.ticketplusserver.dto;

/**
 * DTO para asignar un agente concreto a un ticket.
 *
 * Ejemplo de JSON recibido:
 * {
 *   "agentUsername": "erik"
 * }
 */
public class AssignAgentRequest {
 
    private String agentUsername;
 
    public AssignAgentRequest() {}
 
    public String getAgentUsername()                    { return agentUsername; }
    public void setAgentUsername(String agentUsername)  { this.agentUsername = agentUsername; }
}
 