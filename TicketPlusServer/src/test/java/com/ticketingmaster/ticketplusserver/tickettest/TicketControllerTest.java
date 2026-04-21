package com.ticketingmaster.ticketplusserver.tickettest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketingmaster.ticketplusserver.dto.LoginRequest;
import com.ticketingmaster.ticketplusserver.dto.TicketRequest;
import com.ticketingmaster.ticketplusserver.model.Priority;
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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para TicketController.
 * Levanta el contexto Spring completo contra PostgreSQL real.
 * Requiere que existan los usuarios 'admin' y 'user1' en la BD.
 *
 * Coloca en: src/test/java/com/ticketingmaster/ticketplusserver/
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TicketControllerTest {

    @Autowired private MockMvc                  mockMvc;
    @Autowired private ObjectMapper             objectMapper;
    @Autowired private TicketRepo               ticketRepo;
    @Autowired private TokenBlacklistRepository blacklistRepo;

    private String adminToken;
    private String userToken;
    private Long   ticketId;

    // ─── Setup ────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws Exception {
        blacklistRepo.deleteAll();
        ticketRepo.deleteAll();
        adminToken = obtenerToken("admin", "admin123");
        userToken  = obtenerToken("david", "admin123");
        ticketId   = crearTicket(userToken);
    }

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

    //  POST /api/tickets — Crear ticket

    @Nested
    @DisplayName("POST /api/tickets — Crear ticket")
    class CrearTicket {

        @Test
        @DisplayName("USER autenticado crea ticket → 201 con payload completo")
        void crear_userAutenticado_devuelve201() throws Exception {
            TicketRequest req = new TicketRequest();
            req.setTitle("Solicitud monitor");
            req.setDescription("Necesito un segundo monitor");
            req.setPriority(Priority.MEDIUM);

            mockMvc.perform(post("/api/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.ref").value(org.hamcrest.Matchers.startsWith("INC-")))
                    .andExpect(jsonPath("$.title").value("Solicitud monitor"))
                    .andExpect(jsonPath("$.priority").value("MEDIUM"))
                    .andExpect(jsonPath("$.status").value("Opened"))
                    .andExpect(jsonPath("$.createdBy").value("user1"))
                    .andExpect(jsonPath("$.agent").isEmpty())
                    .andExpect(jsonPath("$.createdAt").isNotEmpty());
        }

        @Test
        @DisplayName("ADMIN autenticado crea ticket → 201")
        void crear_adminAutenticado_devuelve201() throws Exception {
            TicketRequest req = new TicketRequest();
            req.setTitle("Ticket de admin");
            req.setDescription("Descripción");
            req.setPriority(Priority.LOW);

            mockMvc.perform(post("/api/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.createdBy").value("admin"));
        }

        @Test
        @DisplayName("Sin token → 403 Forbidden")
        void crear_sinToken_devuelve403() throws Exception {
            TicketRequest req = new TicketRequest();
            req.setTitle("Ticket sin auth");
            req.setDescription("Descripción");
            req.setPriority(Priority.LOW);

            mockMvc.perform(post("/api/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden());
        }
    }

    //  GET /api/tickets — Listar tickets

    @Nested
    @DisplayName("GET /api/tickets — Listar tickets")
    class ListarTickets {

        @Test
        @DisplayName("ADMIN → recibe todos los tickets del sistema")
        void listar_admin_recibeTodos() throws Exception {
            mockMvc.perform(get("/api/tickets")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("USER → recibe solo sus propios tickets")
        void listar_user_recibeLosSuyos() throws Exception {
            mockMvc.perform(get("/api/tickets")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].createdBy").value("user1"));
        }

        @Test
        @DisplayName("Sin token → 403 Forbidden")
        void listar_sinToken_devuelve403() throws Exception {
            mockMvc.perform(get("/api/tickets"))
                    .andExpect(status().isForbidden());
        }
    }

    //  GET /api/tickets/{id} — Detalle de ticket

    @Nested
    @DisplayName("GET /api/tickets/{id} — Detalle de ticket")
    class DetalleTicket {

        @Test
        @DisplayName("USER propietario → 200 con datos del ticket")
        void detalle_userPropietario_devuelve200() throws Exception {
            mockMvc.perform(get("/api/tickets/" + ticketId)
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ticketId))
                    .andExpect(jsonPath("$.title").value("PC no enciende"));
        }

        @Test
        @DisplayName("ADMIN → puede ver cualquier ticket")
        void detalle_admin_puedeVerCualquierTicket() throws Exception {
            mockMvc.perform(get("/api/tickets/" + ticketId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ticketId));
        }

        @Test
        @DisplayName("Ticket inexistente → 404 Not Found")
        void detalle_ticketInexistente_devuelve404() throws Exception {
            mockMvc.perform(get("/api/tickets/99999")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }

    //  PATCH /api/tickets/{id}/assign — Asignarme el ticket

    @Nested
    @DisplayName("PATCH /api/tickets/{id}/assign — Asignarme el ticket")
    class AsignarAgente {

        @Test
        @DisplayName("ADMIN se asigna el ticket → 200 con agent=admin y status=In Progress")
        void assign_admin_devuelve200() throws Exception {
            mockMvc.perform(patch("/api/tickets/" + ticketId + "/assign")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.agent").value("admin"))
                    .andExpect(jsonPath("$.status").value("In Progress"));
        }

        @Test
        @DisplayName("USER intenta asignarse el ticket → 403 Forbidden")
        void assign_user_devuelve403() throws Exception {
            mockMvc.perform(patch("/api/tickets/" + ticketId + "/assign")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }
    }

    //  PATCH /api/tickets/{id}/agent — Asignar a otro agente

    @Nested
    @DisplayName("PATCH /api/tickets/{id}/agent — Asignar a otro agente")
    class AsignarOtroAgente {

        @Test
        @DisplayName("ADMIN asigna ticket a admin → 200 con agent correcto")
        void agent_admin_devuelve200() throws Exception {
            mockMvc.perform(patch("/api/tickets/" + ticketId + "/agent")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"agentUsername\": \"admin\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.agent").value("admin"))
                    .andExpect(jsonPath("$.status").value("In Progress"));
        }

        @Test
        @DisplayName("Agente inexistente → 404 Not Found")
        void agent_agenteInexistente_devuelve404() throws Exception {
            mockMvc.perform(patch("/api/tickets/" + ticketId + "/agent")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"agentUsername\": \"noexiste\"}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("USER intenta asignar agente → 403 Forbidden")
        void agent_user_devuelve403() throws Exception {
            mockMvc.perform(patch("/api/tickets/" + ticketId + "/agent")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content("{\"agentUsername\": \"admin\"}"))
                    .andExpect(status().isForbidden());
        }
    }

    //  PATCH /api/tickets/{id}/status — Cambiar estado

    @Nested
    @DisplayName("PATCH /api/tickets/{id}/status — Cambiar estado")
    class CambiarEstado {

        @Test
        @DisplayName("ADMIN cambia estado a 'In Progress' → 200")
        void status_adminCambiaEstado_devuelve200() throws Exception {
            mockMvc.perform(patch("/api/tickets/" + ticketId + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"status\": \"In Progress\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("In Progress"));
        }

        @Test
        @DisplayName("ADMIN cambia estado a 'Closed' → 200")
        void status_adminCierraTicker_devuelve200() throws Exception {
            mockMvc.perform(patch("/api/tickets/" + ticketId + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"status\": \"Closed\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("Closed"));
        }

        @Test
        @DisplayName("Estado inválido → 400 Bad Request")
        void status_estadoInvalido_devuelve400() throws Exception {
            mockMvc.perform(patch("/api/tickets/" + ticketId + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"status\": \"Inventado\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("USER intenta cambiar estado → 403 Forbidden")
        void status_user_devuelve403() throws Exception {
            mockMvc.perform(patch("/api/tickets/" + ticketId + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content("{\"status\": \"In Progress\"}"))
                    .andExpect(status().isForbidden());
        }
    }

    //  PATCH /api/tickets/{id}/priority — Cambiar prioridad

    @Nested
    @DisplayName("PATCH /api/tickets/{id}/priority — Cambiar prioridad")
    class CambiarPrioridad {

        @Test
        @DisplayName("USER cambia prioridad de su ticket → 200")
        void priority_userCambiaSuTicket_devuelve200() throws Exception {
            mockMvc.perform(patch("/api/tickets/" + ticketId + "/priority")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content("{\"priority\": \"LOW\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priority").value("LOW"));
        }

        @Test
        @DisplayName("ADMIN cambia prioridad de cualquier ticket → 200")
        void priority_adminCualquierTicket_devuelve200() throws Exception {
            mockMvc.perform(patch("/api/tickets/" + ticketId + "/priority")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"priority\": \"CRITICAL\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priority").value("CRITICAL"));
        }

        @Test
        @DisplayName("Prioridad inválida → 400 Bad Request")
        void priority_invalida_devuelve400() throws Exception {
            mockMvc.perform(patch("/api/tickets/" + ticketId + "/priority")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"priority\": \"URGENTE\"}"))
                    .andExpect(status().isBadRequest());
        }
    }
 
    //  PATCH /api/tickets/{id}/close — Cerrar ticket

    @Nested
    @DisplayName("PATCH /api/tickets/{id}/close — Cerrar ticket")
    class CerrarTicket {

        @Test
        @DisplayName("USER propietario cierra su ticket → 200 con status Closed")
        void close_userPropietario_devuelve200() throws Exception {
            mockMvc.perform(patch("/api/tickets/" + ticketId + "/close")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("Closed"));
        }

        @Test
        @DisplayName("ADMIN cierra cualquier ticket → 200")
        void close_admin_devuelve200() throws Exception {
            mockMvc.perform(patch("/api/tickets/" + ticketId + "/close")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("Closed"));
        }

        @Test
        @DisplayName("Ticket inexistente → 404 Not Found")
        void close_ticketInexistente_devuelve404() throws Exception {
            mockMvc.perform(patch("/api/tickets/99999/close")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }
}