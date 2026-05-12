package com.ticketingmaster.ticketplusserver.usertest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketingmaster.ticketplusserver.dto.LoginRequest;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para UserController.
 * Cubre GET /api/users, POST /api/users, DELETE /api/users/{username}
 * y GET /api/users/agents.
 *
 * @author David Busquet
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired private MockMvc                  mockMvc;
    @Autowired private ObjectMapper             objectMapper;
    @Autowired private TokenBlacklistRepository blacklistRepo;
    @Autowired private DetailTicketRepo         detailRepo;
    @Autowired private TicketRepo               ticketRepo;
    @Autowired private UserRepo                 userRepo;

    private String adminToken;
    private String userToken;

    //Setup

    @BeforeEach
    void setUp() throws Exception {
        blacklistRepo.deleteAll();
        detailRepo.deleteAll();
        ticketRepo.deleteAll();
        adminToken = obtenerToken("admin", "admin123");
        userToken  = obtenerToken("david", "david123");
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

    //  GET /api/users — Listar todos los usuarios activos

    @Nested
    @DisplayName("GET /api/users — Listar todos los usuarios activos")
    class ListarUsuarios {

        @Test
        @DisplayName("ADMIN → 200 con lista de usuarios activos")
        void listar_admin_devuelve200() throws Exception {
            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                    .andExpect(jsonPath("$[0].id").isNumber())
                    .andExpect(jsonPath("$[0].username").isNotEmpty())
                    .andExpect(jsonPath("$[0].role").isNotEmpty())
                    .andExpect(jsonPath("$[0].active").value(true));
        }

        @Test
        @DisplayName("ADMIN → la respuesta nunca contiene passwordHash")
        void listar_admin_noExponeCamposSensibles() throws Exception {
            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                    .andExpect(jsonPath("$[0].password").doesNotExist());
        }

        @Test
        @DisplayName("ADMIN → solo devuelve usuarios activos")
        void listar_admin_soloActivos() throws Exception {
            // Crear y desactivar un usuario
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .content("{\"username\":\"temporal\",\"password\":\"pass123\",\"role\":\"USER\"}"))
                    .andExpect(status().isCreated());

            mockMvc.perform(delete("/api/users/temporal")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());

            // El usuario desactivado no debe aparecer en la lista
            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.username == 'temporal')]").isEmpty());
        }

        @Test
        @DisplayName("USER → 403 Forbidden")
        void listar_user_devuelve403() throws Exception {
            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Sin token → 403 Forbidden")
        void listar_sinToken_devuelve403() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isForbidden());
        }
    }

    //  POST /api/users — Crear usuario

    @Nested
    @DisplayName("POST /api/users — Crear usuario")
    class CrearUsuario {

        @Test
        @DisplayName("ADMIN crea USER → 201 con datos correctos")
        void crear_adminCreaUser_devuelve201() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"username\":\"nuevouser\",\"password\":\"pass123\",\"role\":\"USER\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("nuevouser"))
                    .andExpect(jsonPath("$.role").value("USER"))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());

            // Limpieza
            mockMvc.perform(delete("/api/users/nuevouser")
                    .header("Authorization", "Bearer " + adminToken));
        }

        @Test
        @DisplayName("ADMIN crea ADMIN → 201 con rol ADMIN")
        void crear_adminCreaAdmin_devuelve201() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"username\":\"nuevoadmin\",\"password\":\"pass123\",\"role\":\"ADMIN\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.role").value("ADMIN"));

            // Limpieza
            mockMvc.perform(delete("/api/users/nuevoadmin")
                    .header("Authorization", "Bearer " + adminToken));
        }

        @Test
        @DisplayName("Username ya existente y activo → 400 Bad Request")
        void crear_usernameExistente_devuelve400() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"username\":\"admin\",\"password\":\"pass123\",\"role\":\"ADMIN\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Reactivar usuario desactivado → 201 con active=true")
        void crear_reactivaUsuarioDesactivado_devuelve201() throws Exception {
            // Crear y desactivar
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .content("{\"username\":\"reactivable\",\"password\":\"pass123\",\"role\":\"USER\"}"))
                    .andExpect(status().isCreated());

            mockMvc.perform(delete("/api/users/reactivable")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());

            // Reactivar
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"username\":\"reactivable\",\"password\":\"nuevapass\",\"role\":\"USER\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("reactivable"))
                    .andExpect(jsonPath("$.active").value(true));

            // Limpieza
            mockMvc.perform(delete("/api/users/reactivable")
                    .header("Authorization", "Bearer " + adminToken));
        }

        @Test
        @DisplayName("Rol inválido → 400 Bad Request")
        void crear_rolInvalido_devuelve400() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("{\"username\":\"test\",\"password\":\"pass123\",\"role\":\"SUPERADMIN\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("USER intenta crear usuario → 403 Forbidden")
        void crear_user_devuelve403() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content("{\"username\":\"test\",\"password\":\"pass123\",\"role\":\"USER\"}"))
                    .andExpect(status().isForbidden());
        }
    }

    //  DELETE /api/users/{username} — Eliminación lógica

    @Nested
    @DisplayName("DELETE /api/users/{username} — Eliminación lógica")
    class EliminarUsuario {

        @Test
        @DisplayName("ADMIN elimina USER → 204 No Content")
        void eliminar_adminEliminaUser_devuelve204() throws Exception {
            // Crear usuario
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .content("{\"username\":\"paraeliminar\",\"password\":\"pass123\",\"role\":\"USER\"}"))
                    .andExpect(status().isCreated());

            // Eliminar
            mockMvc.perform(delete("/api/users/paraeliminar")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("ADMIN intenta eliminarse a sí mismo → 400 Bad Request")
        void eliminar_aSiMismo_devuelve400() throws Exception {
            mockMvc.perform(delete("/api/users/admin")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Eliminar usuario inexistente → 404 Not Found")
        void eliminar_usuarioInexistente_devuelve404() throws Exception {
            mockMvc.perform(delete("/api/users/noexiste")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("USER intenta eliminar usuario → 403 Forbidden")
        void eliminar_user_devuelve403() throws Exception {
            mockMvc.perform(delete("/api/users/admin")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }
    }

    //  GET /api/users/agents — Listar agentes

    @Nested
    @DisplayName("GET /api/users/agents — Listar agentes")
    class ListarAgentes {

        @Test
        @DisplayName("ADMIN → 200 con lista de agentes activos")
        void listar_admin_devuelve200ConAgentes() throws Exception {
            mockMvc.perform(get("/api/users/agents")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                    .andExpect(jsonPath("$[*].role", everyItem(is("ADMIN"))));
        }

        @Test
        @DisplayName("ADMIN → la respuesta no contiene passwordHash ni active")
        void listar_admin_noExponeCamposSensibles() throws Exception {
            mockMvc.perform(get("/api/users/agents")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                    .andExpect(jsonPath("$[0].active").doesNotExist());
        }

        @Test
        @DisplayName("ADMIN → el agente 'admin' aparece en la lista")
        void listar_admin_contieneAdminPorDefecto() throws Exception {
            mockMvc.perform(get("/api/users/agents")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.username == 'admin')]").exists());
        }

        @Test
        @DisplayName("USER → 403 Forbidden")
        void listar_user_devuelve403() throws Exception {
            mockMvc.perform(get("/api/users/agents")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Sin token → 403 Forbidden")
        void listar_sinToken_devuelve403() throws Exception {
            mockMvc.perform(get("/api/users/agents"))
                    .andExpect(status().isForbidden());
        }
    }
}