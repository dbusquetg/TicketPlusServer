package com.ticketingmaster.ticketplusserver.tokentest;

import com.ticketingmaster.ticketplusserver.model.TokenBlacklist;
import com.ticketingmaster.ticketplusserver.repo.TokenBlacklistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas de integración de TokenBlacklistRepository contra PostgreSQL real.
 *
 * A diferencia de UserRepo, aquí NO usamos @Transactional porque
 * deleteByExpiresAtBefore necesita que los datos estén committed
 * para poder borrarlos y verificar el resultado correctamente.
 *
 * En su lugar, cada test limpia la blacklist en @BeforeEach.
 *
 * Si PostgreSQL no está arrancado, los tests fallan al iniciar.
 *
 * Coloca este fichero en:
 * src/test/java/com/ticketingmaster/ticketplusserver/
 */
@SpringBootTest
class TokenBlacklistIntegrationTest {

    @Autowired
    private TokenBlacklistRepository blacklistRepo;

    private static final String TOKEN_A = "headerA.payloadA.signatureA";
    private static final String TOKEN_B = "headerB.payloadB.signatureB";

    @BeforeEach
    void limpiarBlacklist() {
        blacklistRepo.deleteAll();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private TokenBlacklist guardar(String token, LocalDateTime expiresAt) {
        return blacklistRepo.save(new TokenBlacklist(token, expiresAt));
    }

    private LocalDateTime futuro()  { return LocalDateTime.now().plusHours(1);  }
    private LocalDateTime pasado()  { return LocalDateTime.now().minusHours(1); }

    // ════════════════════════════════════════════════════════════════════════
    //  CONEXIÓN
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Conexión a la base de datos")
    class ConexionTests {

        @Test
        @DisplayName("El repositorio está disponible y PostgreSQL responde")
        void repositorio_disponible_postgresResponde() {
            assertThat(blacklistRepo).isNotNull();
            assertThat(blacklistRepo.count()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Se puede guardar un token en la blacklist — BD operativa")
        void guardarToken_bdOperativa() {
            TokenBlacklist guardado = guardar(TOKEN_A, futuro());

            assertThat(guardado.getTokenHash()).isEqualTo(TOKEN_A);
            assertThat(blacklistRepo.count()).isEqualTo(1);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  existsByTokenHash — consultado por JwtAuthFilter en cada request
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("existsByTokenHash")
    class ExistsByTokenHashTests {

        @Test
        @DisplayName("Token en blacklist → devuelve true")
        void existsByTokenHash_tokenEnBlacklist_devuelveTrue() {
            guardar(TOKEN_A, futuro());

            assertThat(blacklistRepo.existsByTokenHash(TOKEN_A)).isTrue();
        }

        @Test
        @DisplayName("Token no en blacklist → devuelve false")
        void existsByTokenHash_tokenNoEnBlacklist_devuelveFalse() {
            assertThat(blacklistRepo.existsByTokenHash(TOKEN_A)).isFalse();
        }

        @Test
        @DisplayName("Tokens distintos no se confunden entre sí")
        void existsByTokenHash_tokenesDistintos_noSeConfunden() {
            guardar(TOKEN_A, futuro());

            assertThat(blacklistRepo.existsByTokenHash(TOKEN_A)).isTrue();
            assertThat(blacklistRepo.existsByTokenHash(TOKEN_B)).isFalse();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  deleteByExpiresAtBefore — ejecutado por TokenCleanupTask cada hora
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteByExpiresAtBefore (limpieza automática)")
    class DeleteExpiredTests {

        @Test
        @DisplayName("Token expirado → es eliminado por la limpieza")
        void deleteByExpiresAtBefore_eliminaTokenExpirado() {
            guardar(TOKEN_A, pasado());

            blacklistRepo.deleteByExpiresAtBefore(LocalDateTime.now());

            assertThat(blacklistRepo.existsByTokenHash(TOKEN_A)).isFalse();
        }

        @Test
        @DisplayName("Token vigente → no es eliminado por la limpieza")
        void deleteByExpiresAtBefore_noEliminaTokenVigente() {
            guardar(TOKEN_B, futuro());

            blacklistRepo.deleteByExpiresAtBefore(LocalDateTime.now());

            assertThat(blacklistRepo.existsByTokenHash(TOKEN_B)).isTrue();
        }

        @Test
        @DisplayName("Mezcla expirado y vigente → solo elimina el expirado")
        void deleteByExpiresAtBefore_soloEliminaExpirados() {
            guardar(TOKEN_A, pasado());
            guardar(TOKEN_B, futuro());

            blacklistRepo.deleteByExpiresAtBefore(LocalDateTime.now());

            assertThat(blacklistRepo.count()).isEqualTo(1);
            assertThat(blacklistRepo.existsByTokenHash(TOKEN_A)).isFalse();
            assertThat(blacklistRepo.existsByTokenHash(TOKEN_B)).isTrue();
        }

        @Test
        @DisplayName("BD vacía → la limpieza no lanza excepción")
        void deleteByExpiresAtBefore_bdVacia_noLanzaExcepcion() {
            assertThatNoException().isThrownBy(() ->
                blacklistRepo.deleteByExpiresAtBefore(LocalDateTime.now())
            );
        }
    }
}
