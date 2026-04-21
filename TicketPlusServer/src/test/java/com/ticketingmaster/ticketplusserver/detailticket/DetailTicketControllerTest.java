package com.ticketingmaster.ticketplusserver.detailticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketingmaster.ticketplusserver.dto.LoginRequest;
import com.ticketingmaster.ticketplusserver.dto.TicketRequest;
import com.ticketingmaster.ticketplusserver.model.Priority;
import com.ticketingmaster.ticketplusserver.repo.DetailTicketRepo;
import com.ticketingmaster.ticketplusserver.repo.TicketRepo;
import com.ticketingmaster.ticketplusserver.repo.TokenBlacklistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para DetailTicketController.
 * Cubre los endpoints de hilo (/details) y comentarios (/comments).
 *
 * @Author David Busquet
 */
@SpringBootTest
@AutoConfigureMockMvc
class DetailTicketControllerTest {

    @Autowired private MockMvc                  mockMvc;
    @Autowired private ObjectMapper             objectMapper;
    @Autowired private TicketRepo               ticketRepo;
    @Autowired private DetailTicketRepo         detailRepo;
    @Autowired private TokenBlacklistRepository blacklistRepo;

    private String adminToken;
    private String userToken;
    private Long   ticketId;

    //  Setup 

    @BeforeEach
    void setUp() throws Exception {
        blacklistRepo.deleteAll();
        detailRepo.deleteAll();
        ticketRepo.deleteAll();
        adminToken = obtenerToken("admin", "admin123");
        userToken  = obtenerToken("david", "admin123");
        ticketId   = crearTicket(userToken);
    }
    /**
     * Obtiene token para poder realizar los tests
     * @param username Nombre de usuario de la BD
     * @param password Contraseña del usuario
     * @return Token del usuario.
     * @throws Exception 
     */
    private String obtenerToken(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }
    
    // POST /api/tickets «Crear Ticket» Crear ticket 
    private Long crearTicket(String token) throws Exception {
        TicketRequest req = new TicketRequest();
        req.setTitle("PC no enciende");
        req.setDescription("Desde ayer no arranca");
        req.setPriority(Priority.HIGH);

        MvcResult result = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    //  POST /api/tickets/{id}/details — Añadir entrada al hilo

    @Nested
    @DisplayName("POST /api/tickets/{id}/details — Añadir entrada al hilo")
    class AñadirDetalle {

        @Test
        @DisplayName("USER añade entrada → 201 con type T")
        void añadir_user_devuelve201ConTipoT() throws Exception {
            mockMvc.perform(post("/api/tickets/" + ticketId + "/details")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content("{\"contentDetail\": \"El problema sigue igual\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.typeDetail").value("T"))
                    .andExpect(jsonPath("$.author").value("user1"))
                    .andExpect(jsonPath("$.contentDetail").value("El problema sigue igual"));
        }

        @Test
        @DisplayName("ADMIN añade entrada → 201 con type R")
        void añadir_admin_devuelve201ConTipoR() throws Exception {
            mockMvc.perform(post("/api/tickets/" + ticketId + "/details")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"contentDetail\": \"Revisamos el equipo mañana\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.typeDetail").value("R"))
                    .andExpect(jsonPath("$.author").value("admin"));
        }

        @Test
        @DisplayName("Ticket inexistente → 404 Not Found")
        void añadir_ticketInexistente_devuelve404() throws Exception {
            mockMvc.perform(post("/api/tickets/99999/details")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content("{\"contentDetail\": \"Mensaje\"}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Sin token → 403 Forbidden")
        void añadir_sinToken_devuelve403() throws Exception {
            mockMvc.perform(post("/api/tickets/" + ticketId + "/details")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"contentDetail\": \"Mensaje\"}"))
                    .andExpect(status().isForbidden());
        }
    }


    //  GET /api/tickets/{id}/details — Ver hilo completo

    @Nested
    @DisplayName("GET /api/tickets/{id}/details — Ver hilo completo")
    class ObtenerHilo {

        @Test
        @DisplayName("Hilo vacío → 200 con lista vacía")
        void hilo_vacio_devuelveLista() throws Exception {
            mockMvc.perform(get("/api/tickets/" + ticketId + "/details")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Hilo con entradas → 200 ordenado cronológicamente")
        void hilo_conEntradas_devuelveOrdenado() throws Exception {
            mockMvc.perform(post("/api/tickets/" + ticketId + "/details")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + userToken)
                    .content("{\"contentDetail\": \"Primera entrada\"}"));

            mockMvc.perform(post("/api/tickets/" + ticketId + "/details")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .content("{\"contentDetail\": \"Segunda entrada\"}"));

            mockMvc.perform(get("/api/tickets/" + ticketId + "/details")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].typeDetail").value("T"))
                    .andExpect(jsonPath("$[1].typeDetail").value("R"));
        }

        @Test
        @DisplayName("Ticket inexistente → 404 Not Found")
        void hilo_ticketInexistente_devuelve404() throws Exception {
            mockMvc.perform(get("/api/tickets/99999/details")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }

    //  POST /api/tickets/{id}/comments — Añadir comentario

    @Nested
    @DisplayName("POST /api/tickets/{id}/comments — Añadir comentario")
    class AñadirComentario {

        @Test
        @DisplayName("USER añade comentario → 201 con ticketRef, ticketTitle, author y content")
        void comentario_user_devuelve201() throws Exception {
            mockMvc.perform(post("/api/tickets/" + ticketId + "/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content("{\"contentDetail\": \"Puedes revisarlo hoy?\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.ticketRef").value("INC-" + ticketId))
                    .andExpect(jsonPath("$.ticketTitle").value("PC no enciende"))
                    .andExpect(jsonPath("$.author").value("user1"))
                    .andExpect(jsonPath("$.content").value("Puedes revisarlo hoy?"))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty());
        }

        @Test
        @DisplayName("ADMIN añade comentario → 201")
        void comentario_admin_devuelve201() throws Exception {
            mockMvc.perform(post("/api/tickets/" + ticketId + "/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"contentDetail\": \"Revisamos mañana\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.author").value("admin"));
        }

        @Test
        @DisplayName("Ticket inexistente → 404 Not Found")
        void comentario_ticketInexistente_devuelve404() throws Exception {
            mockMvc.perform(post("/api/tickets/99999/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content("{\"contentDetail\": \"Mensaje\"}"))
                    .andExpect(status().isNotFound());
        }
    }

    //  GET /api/tickets/{id}/comments — Obtener comentarios

    @Nested
    @DisplayName("GET /api/tickets/{id}/comments — Obtener comentarios")
    class ObtenerComentarios {

        @Test
        @DisplayName("Sin comentarios → 200 con lista vacía")
        void comentarios_sinComentarios_devuelveLista() throws Exception {
            mockMvc.perform(get("/api/tickets/" + ticketId + "/comments")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Con comentarios → 200 ordenados cronológicamente")
        void comentarios_conComentarios_devuelveOrdenados() throws Exception {
            mockMvc.perform(post("/api/tickets/" + ticketId + "/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + userToken)
                    .content("{\"contentDetail\": \"Primer comentario\"}"));

            mockMvc.perform(post("/api/tickets/" + ticketId + "/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .content("{\"contentDetail\": \"Segundo comentario\"}"));

            mockMvc.perform(get("/api/tickets/" + ticketId + "/comments")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].author").value("user1"))
                    .andExpect(jsonPath("$[1].author").value("admin"))
                    .andExpect(jsonPath("$[0].content").value("Primer comentario"));
        }

        @Test
        @DisplayName("Ticket inexistente → 404 Not Found")
        void comentarios_ticketInexistente_devuelve404() throws Exception {
            mockMvc.perform(get("/api/tickets/99999/comments")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Sin token → 403 Forbidden")
        void comentarios_sinToken_devuelve403() throws Exception {
            mockMvc.perform(get("/api/tickets/" + ticketId + "/comments"))
                    .andExpect(status().isForbidden());
        }
    }
}