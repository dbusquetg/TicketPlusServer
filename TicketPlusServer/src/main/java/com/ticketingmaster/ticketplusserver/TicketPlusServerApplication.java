package com.ticketingmaster.ticketplusserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * Clase principal donde se inicia el servidor, contiene la función main.
 * Marcada con SpringBootApplication como punto de entrada del framework.
 * @author David Busquet
 */
@SpringBootApplication
public class TicketPlusServerApplication {
    
    /**
     * Función main inicial del programa.
     * @param args Argumentos de entrada en formato String[].
     */
    public static void main(String[] args) {
        SpringApplication.run(TicketPlusServerApplication.class, args);
    }

}