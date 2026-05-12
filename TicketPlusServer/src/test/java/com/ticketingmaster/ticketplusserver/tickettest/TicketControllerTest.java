package com.ticketingmaster.ticketplusserver.tickettest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketingmaster.ticketplusserver.dto.LoginRequest;
import com.ticketingmaster.ticketplusserver.dto.TicketRequest;
import com.ticketingmaster.ticketplusserver.model.Priority;
import com.ticketingmaster.ticketplusserver.repo.DetailTicketRepo;
import com.ticketingmaster.ticketplusserver.repo.TicketRepo;
import com.ticketingmaster.ticketplusserver.repo.TokenBlacklistRepository;
import com.ticketingmaster.ticketplusserver.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para TicketController.
 * Cubre todos los endpoints de tickets incluyendo:
 * - Campo points (score del creador) en las respuestas
 * - Campo resolvedAt (closedDate) al cerrar tickets
 * - Descuento de 5 puntos al crear un ticket
 *
 * @author David Busquet
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TicketControllerTest {

    @Autowired private WebApplicationContext    context;
    private MockMvc                             mockMvc;
    @Autowired private ObjectMapper             objectMapper;
    @Autowired private TicketRepo               ticketRepo;
    @Autowired private DetailTicketRepo         detailRepo;
    @Autowired private TokenBlacklistRepository blacklistRepo;
    @Autowired private UserRepo                 userRepo;

    private String adminToken;
    private String userToken;

    // Setup

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .defaultRequest(get("/").secure(true))
                .build();
        blacklistRepo.deleteAll();
        detailRepo.deleteAll();
        ticketRepo.deleteAll();
        adminToken = obtenerToken("admin", "admin123");
        userToken  = obtenerToken("david", "david123");

        // Resetear el score de david a 100 para que los tests sean predecibles
        userRepo.findByUsername("david").ifPresent(u -> {
            u.setScore(100);
            userRepo.save(u);
        });
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

    //  POST /api/tickets — Crear ticket + descuento de score

    @Nested
    @DisplayName("POST /api/tickets — Crear ticket")
    class CrearTicket {

        @Test
        @DisplayName("USER crea ticket → 201 con payload completo incluyendo points")
        void crear_userAutenticado_devuelve201ConPoints() throws Exception {
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
                    .andExpect(jsonPath("$.ref").value(startsWith("INC-")))
                    .andExpect(jsonPath("$.title").value("Solicitud monitor"))
                    .andExpect(jsonPath("$.priority").value("MEDIUM"))
                    .andExpect(jsonPath("$.status").value("Opened"))
                    .andExpect(jsonPath("$.createdBy").value("david"))
                    .andExpect(jsonPath("$.points").isNumber())
                    .andExpect(jsonPath("$.agent").isEmpty())
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.resolvedAt").isEmpty());
        }

        @Test
        @DisplayName("Crear ticket resta 5 puntos al score del creador")
        void crear_ticket_resta5PuntosAlCreador() throws Exception {
            // Score inicial = 100 (reseteado en setUp)
            TicketRequest req = new TicketRequest();
            req.setTitle("Ticket test score");
            req.setDescription("Test");
            req.setPriority(Priority.LOW);

            MvcResult result = mockMvc.perform(post("/api/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andReturn();

            // El campo points en la respuesta debe ser 95 (100 - 5)
            int points = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("points").asInt();
            assert points == 95 : "Score esperado 95, obtenido " + points;
        }

        @Test
        @DisplayName("Score no baja de 0 aunque se creen muchos tickets")
        void crear_ticket_scoreNoBajaDeZero() throws Exception {
            // Forzar score a 3 para que no pueda bajar más de 5
            userRepo.findByUsername("david").ifPresent(u -> {
                u.setScore(3);
                userRepo.save(u);
            });

            TicketRequest req = new TicketRequest();
            req.setTitle("Ticket con score bajo");
            req.setDescription("Test");
            req.setPriority(Priority.LOW);

            MvcResult result = mockMvc.perform(post("/api/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andReturn();

            // El campo points debe ser 0, nunca negativo
            int points = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("points").asInt();
            assert points == 0 : "Score esperado 0, obtenido " + points;
        }

        @Test
        @DisplayName("Sin token → 403 Forbidden")
        void crear_sinToken_devuelve403() throws Exception {
            TicketRequest req = new TicketRequest();
            req.setTitle("Ticket sin auth");
            req.setDescription("Test");
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
        @DisplayName("ADMIN → recibe todos los tickets con campo points")
        void listar_admin_recibeTodosConPoints() throws Exception {
            crearTicket(userToken);

            mockMvc.perform(get("/api/tickets")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                    .andExpect(jsonPath("$[0].points").isNumber())
                    .andExpect(jsonPath("$[0].resolvedAt").isEmpty());
        }

        @Test
        @DisplayName("USER → recibe solo sus tickets con campo points")
        void listar_user_recibeLosSuyosConPoints() throws Exception {
            crearTicket(userToken);

            mockMvc.perform(get("/api/tickets")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].createdBy").value("david"))
                    .andExpect(jsonPath("$[0].points").isNumber());
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
        @DisplayName("USER propietario → 200 con points y resolvedAt null")
        void detalle_userPropietario_devuelve200ConPointsYResolvedAt() throws Exception {
            Long id = crearTicket(userToken);

            mockMvc.perform(get("/api/tickets/" + id)
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.points").isNumber())
                    .andExpect(jsonPath("$.resolvedAt").isEmpty());
        }

        @Test
        @DisplayName("ADMIN → puede ver cualquier ticket con todos los campos")
        void detalle_admin_puedeVerCualquierTicket() throws Exception {
            Long id = crearTicket(userToken);

            mockMvc.perform(get("/api/tickets/" + id)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.points").isNumber())
                    .andExpect(jsonPath("$.createdAt").isNotEmpty());
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
        @DisplayName("ADMIN se asigna → 200 con agent=admin y status=In Progress")
        void assign_admin_devuelve200() throws Exception {
            Long id = crearTicket(userToken);

            mockMvc.perform(patch("/api/tickets/" + id + "/assign")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.agent").value("admin"))
                    .andExpect(jsonPath("$.status").value("In Progress"));
        }

        @Test
        @DisplayName("USER intenta asignarse → 403 Forbidden")
        void assign_user_devuelve403() throws Exception {
            Long id = crearTicket(userToken);

            mockMvc.perform(patch("/api/tickets/" + id + "/assign")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }
    }

    //  PATCH /api/tickets/{id}/agent — Asignar a otro agente

    @Nested
    @DisplayName("PATCH /api/tickets/{id}/agent — Asignar a otro agente")
    class AsignarOtroAgente {

        @Test
        @DisplayName("ADMIN asigna a agente → 200 con agent y status In Progress")
        void agent_admin_devuelve200() throws Exception {
            Long id = crearTicket(userToken);

            mockMvc.perform(patch("/api/tickets/" + id + "/agent")
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
            Long id = crearTicket(userToken);

            mockMvc.perform(patch("/api/tickets/" + id + "/agent")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"agentUsername\": \"noexiste\"}"))
                    .andExpect(status().isNotFound());
        }
    }

    //  PATCH /api/tickets/{id}/status — Cambiar estado

    @Nested
    @DisplayName("PATCH /api/tickets/{id}/status — Cambiar estado")
    class CambiarEstado {

        @Test
        @DisplayName("ADMIN cambia a Closed → resolvedAt se asigna automáticamente")
        void status_closed_asignaResolvedAt() throws Exception {
            Long id = crearTicket(userToken);

            mockMvc.perform(patch("/api/tickets/" + id + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"status\": \"Closed\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("Closed"))
                    .andExpect(jsonPath("$.resolvedAt").isNotEmpty());
        }

        @Test
        @DisplayName("ADMIN cambia a Solved → resolvedAt se asigna automáticamente")
        void status_solved_asignaResolvedAt() throws Exception {
            Long id = crearTicket(userToken);

            mockMvc.perform(patch("/api/tickets/" + id + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"status\": \"Solved\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("Solved"))
                    .andExpect(jsonPath("$.resolvedAt").isNotEmpty());
        }

        @Test
        @DisplayName("ADMIN vuelve a In Progress → resolvedAt se limpia a null")
        void status_inProgress_limpiaResolvedAt() throws Exception {
            Long id = crearTicket(userToken);

            // Primero cerrar
            mockMvc.perform(patch("/api/tickets/" + id + "/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .content("{\"status\": \"Closed\"}"));

            // Luego reabrir
            mockMvc.perform(patch("/api/tickets/" + id + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"status\": \"In Progress\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("In Progress"))
                    .andExpect(jsonPath("$.resolvedAt").isEmpty());
        }

        @Test
        @DisplayName("Estado inválido → 400 Bad Request")
        void status_estadoInvalido_devuelve400() throws Exception {
            Long id = crearTicket(userToken);

            mockMvc.perform(patch("/api/tickets/" + id + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"status\": \"Inventado\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("USER intenta cambiar estado → 403 Forbidden")
        void status_user_devuelve403() throws Exception {
            Long id = crearTicket(userToken);

            mockMvc.perform(patch("/api/tickets/" + id + "/status")
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
            Long id = crearTicket(userToken);

            mockMvc.perform(patch("/api/tickets/" + id + "/priority")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content("{\"priority\": \"LOW\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priority").value("LOW"));
        }

        @Test
        @DisplayName("ADMIN cambia prioridad a CRITICAL → 200")
        void priority_adminCambiaACritical_devuelve200() throws Exception {
            Long id = crearTicket(userToken);

            mockMvc.perform(patch("/api/tickets/" + id + "/priority")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"priority\": \"CRITICAL\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priority").value("CRITICAL"));
        }

        @Test
        @DisplayName("Prioridad inválida → 400 Bad Request")
        void priority_invalida_devuelve400() throws Exception {
            Long id = crearTicket(userToken);

            mockMvc.perform(patch("/api/tickets/" + id + "/priority")
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
        @DisplayName("USER cierra su ticket → 200 con status Closed y resolvedAt asignado")
        void close_userPropietario_devuelve200ConResolvedAt() throws Exception {
            Long id = crearTicket(userToken);

            mockMvc.perform(patch("/api/tickets/" + id + "/close")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("Closed"))
                    .andExpect(jsonPath("$.resolvedAt").isNotEmpty());
        }

        @Test
        @DisplayName("ADMIN cierra cualquier ticket → 200 con resolvedAt asignado")
        void close_admin_devuelve200ConResolvedAt() throws Exception {
            Long id = crearTicket(userToken);

            mockMvc.perform(patch("/api/tickets/" + id + "/close")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("Closed"))
                    .andExpect(jsonPath("$.resolvedAt").isNotEmpty());
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