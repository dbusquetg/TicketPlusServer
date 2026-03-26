package com.ticketingmaster.ticketplusserver.authtest;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración de login y logout.
 *
 * Levanta el contexto Spring completo contra PostgreSQL real.
 * Si el servidor PostgreSQL no está arrancado, los tests fallan al iniciar.
 *
 * Requisito: que existan los usuarios 'admin' y 'user1' en la BD
 * (los crea DataInitializer automáticamente al arrancar la app).
 * @author David Busquet.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired private MockMvc                   mockMvc;
    @Autowired private ObjectMapper              objectMapper;
    @Autowired private TokenBlacklistRepository  blacklistRepo;

    @BeforeEach
    void limpiarBlacklist() {
        blacklistRepo.deleteAll();
    }

    /**
     * Función para hacer login y obtener el token en base a un username y password.
     * @param username nombre de usuario
     * @param password contraseña.
     * @return String del cuerpo de la respuesta.
     * @throws Exception 
     */

    private String loginYObtenToken(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

/**
 * Clase que comprende los tests de login, estos incluyen:
 * Credenciales correctas (admin) → 200 OK con token, rol y username
 * Credenciales correctas (user1) → 200 OK con rol USER
 * Contraseña incorrecta → 401 Unauthorized
 * Usuario inexistente → 401 Unauthorized
 */
    @Nested
    @DisplayName("Login")
    class LoginTests {

        @Test
        @DisplayName("Credenciales correctas (admin) → 200 OK con token, rol y username")
        void login_adminCorrecto_devuelve200() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setUsername("admin");
            req.setPassword("admin123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.role").value("ADMIN"))
                    .andExpect(jsonPath("$.username").value("admin"));
        }

        @Test
        @DisplayName("Credenciales correctas (user1) → 200 OK con rol USER")
        void login_userCorrecto_devuelve200() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setUsername("user1");
            req.setPassword("admin123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.role").value("USER"))
                    .andExpect(jsonPath("$.username").value("user1"));
        }

        @Test
        @DisplayName("Contraseña incorrecta → 401 Unauthorized")
        void login_passwordIncorrecta_devuelve401() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setUsername("admin");
            req.setPassword("passwordMal");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Credenciales incorrectas"));
        }

        @Test
        @DisplayName("Usuario inexistente → 401 Unauthorized")
        void login_usuarioInexistente_devuelve401() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setUsername("noexiste");
            req.setPassword("cualquiera");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Credenciales incorrectas"));
        }

    }

   /**
    * Clase que comprende los tests para el Logout, que comprenden:
    * Token válido → 204 No Content y token guardado en blacklist
    * Token usado en blacklist → 403 en siguiente request
    * Sin header Authorization → 403 Forbidden
    * Token manipulado → 403 Forbidden
    */

    @Nested
    @DisplayName("Logout")
    class LogoutTests {

        @Test
        @DisplayName("Token válido → 204 No Content y token guardado en blacklist")
        void logout_tokenValido_devuelve204YGuardaEnBlacklist() throws Exception {
            String token = loginYObtenToken("admin", "admin123");

            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNoContent());

            assertThat(blacklistRepo.existsByTokenHash(token)).isTrue();
        }

        @Test
        @DisplayName("Token usado en blacklist → 403 en siguiente request")
        void logout_tokenEnBlacklist_siguienteRequestDevuelve403() throws Exception {
            String token = loginYObtenToken("admin", "admin123");

            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNoContent());

            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Sin header Authorization → 403 Forbidden")
        void logout_sinHeader_devuelve403() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Token manipulado → 403 Forbidden")
        void logout_tokenManipulado_devuelve403() throws Exception {
            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer token.falso.manipulado"))
                    .andExpect(status().isForbidden());
        }
    }
}
