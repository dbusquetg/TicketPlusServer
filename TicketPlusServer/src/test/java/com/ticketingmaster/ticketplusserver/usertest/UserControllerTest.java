/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ticketingmaster.ticketplusserver.usertest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketingmaster.ticketplusserver.dto.LoginRequest;
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
 * Tests de integración para UserController.
 * Cubre el endpoint GET /api/users/agents.
 *
 * Coloca en: src/test/java/com/ticketingmaster/ticketplusserver/
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired private MockMvc                  mockMvc;
    @Autowired private ObjectMapper             objectMapper;
    @Autowired private TokenBlacklistRepository blacklistRepo;

    private String adminToken;
    private String userToken;

    // ─── Setup ────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws Exception {
        blacklistRepo.deleteAll();
        adminToken = obtenerToken("admin", "admin123");
        userToken  = obtenerToken("user1", "admin123");
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

    //  GET /api/users/agents — Listar agentes

    @Nested
    @DisplayName("GET /api/users/agents — Listar agentes")
    class ListarAgentes {

        @Test
        @DisplayName("ADMIN → 200 con lista de agentes")
        void listar_admin_devuelve200ConAgentes() throws Exception {
            mockMvc.perform(get("/api/users/agents")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                    .andExpect(jsonPath("$[0].id").isNumber())
                    .andExpect(jsonPath("$[0].username").isNotEmpty())
                    .andExpect(jsonPath("$[0].role").value("ADMIN"));
        }

        @Test
        @DisplayName("ADMIN → la respuesta nunca contiene passwordHash ni active")
        void listar_admin_noExponeCamposSensibles() throws Exception {
            mockMvc.perform(get("/api/users/agents")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                    .andExpect(jsonPath("$[0].active").doesNotExist());
        }

        @Test
        @DisplayName("ADMIN → todos los usuarios devueltos tienen rol ADMIN")
        void listar_admin_todosConRolAdmin() throws Exception {
            mockMvc.perform(get("/api/users/agents")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].role",
                            org.hamcrest.Matchers.everyItem(
                                    org.hamcrest.Matchers.is("ADMIN"))));
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

        @Test
        @DisplayName("Token inválido → 403 Forbidden")
        void listar_tokenInvalido_devuelve403() throws Exception {
            mockMvc.perform(get("/api/users/agents")
                            .header("Authorization", "Bearer token.falso.manipulado"))
                    .andExpect(status().isForbidden());
        }
    }
}