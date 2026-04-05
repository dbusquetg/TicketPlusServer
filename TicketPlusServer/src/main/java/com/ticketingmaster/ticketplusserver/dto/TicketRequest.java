package com.ticketingmaster.ticketplusserver.dto;

import com.ticketingmaster.ticketplusserver.model.Priority;

/**
 * DTO para la creación de un nuevo ticket.
 * El campo createdBy se extrae del JWT en el servicio,
 * no lo envía el cliente directamente.
 */
public class TicketRequest {

    private String  title;
    private String  description;
    private Priority priority;
    private String  typology;
    private String  subTypology;

    public TicketRequest() {}

    public String getTitle()                        { return title; }
    public void setTitle(String title)              { this.title = title; }

    public String getDescription()                  { return description; }
    public void setDescription(String desc)         { this.description = desc; }

    public Priority getPriority()                    { return priority; }
    public void setPriority(Priority priority)       { this.priority = priority; }

    public String getTypology()                     { return typology; }
    public void setTypology(String typology)        { this.typology = typology; }

    public String getSubTypology()                  { return subTypology; }
    public void setSubTypology(String subTypology)  { this.subTypology = subTypology; }
}
