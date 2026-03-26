package com.ticketingmaster.ticketplusserver.userrepotest
       ;

import com.ticketingmaster.ticketplusserver.model.Role;
import com.ticketingmaster.ticketplusserver.model.User;
import com.ticketingmaster.ticketplusserver.repo.UserRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas de integración de UserRepo contra PostgreSQL real.
 *
 * @Transactional hace rollback automático tras cada test,
 * por lo que los usuarios creados durante los tests no persisten en la BD.
 *
 * Los usuarios 'admin' y 'user1' ya existen gracias a DataInitializer
 * y están disponibles para todos los tests de lectura.
 *
 * Si PostgreSQL no está arrancado, los tests fallan al iniciar.
 *
 * @author David Busquet
 */
@SpringBootTest
@Transactional
class UserRepoIntegrationTest {

    @Autowired
    private UserRepo userRepo;

    /**
     * Clase que prueba las conexiones.
     * - El repositorio está disponible y PostgreSQL responde
     * - Se puede guardar y recuperar un usuario — BD operativa
     */
    @Nested
    @DisplayName("Conexión a la base de datos")
    class ConexionTests {

        @Test
        @DisplayName("El repositorio está disponible y PostgreSQL responde")
        void repositorio_disponible_postgresResponde() {
            assertThat(userRepo).isNotNull();
            assertThat(userRepo.count()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Se puede guardar y recuperar un usuario — BD operativa")
        void guardarYRecuperar_bdOperativa() {
            User nuevo = new User("testIntegration", "hashedPwd", Role.USER);
            User guardado = userRepo.save(nuevo);

            assertThat(guardado.getId()).isNotNull();
            assertThat(userRepo.findById(guardado.getId())).isPresent();
        }
    }

    /**
     * Clase per als tests de troba de usuaris mitjançant FindByUsername.
     * -Usuario 'admin' existe en BD → devuelve el usuario con rol ADMIN
     * -Usuario 'user1' existe en BD → devuelve el usuario con rol USER
     * -Usuario inexistente → devuelve Optional vacío
     */
    @Nested
    @DisplayName("findByUsername")
    class FindByUsernameTests {

        @Test
        @DisplayName("Usuario 'admin' existe en BD → devuelve el usuario con rol ADMIN")
        void findByUsername_adminExiste_devuelveAdmin() {
            Optional<User> resultado = userRepo.findByUsername("admin");

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getUsername()).isEqualTo("admin");
            assertThat(resultado.get().getRole()).isEqualTo(Role.ADMIN);
            assertThat(resultado.get().isActive()).isTrue();
        }

        @Test
        @DisplayName("Usuario 'user1' existe en BD → devuelve el usuario con rol USER")
        void findByUsername_user1Existe_devuelveUser() {
            Optional<User> resultado = userRepo.findByUsername("user1");

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getUsername()).isEqualTo("user1");
            assertThat(resultado.get().getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("Usuario inexistente → devuelve Optional vacío")
        void findByUsername_noExiste_devuelveVacio() {
            Optional<User> resultado = userRepo.findByUsername("usuarioQueNoExiste");

            assertThat(resultado).isEmpty();
        }
    }

    /**
     * Clase dedicada a probar si el existByUsername funciona.
     * -'admin' existe → devuelve true
     * -'user1' existe → devuelve true
     * -Usuario inexistente → devuelve false
     */

    @Nested
    @DisplayName("existsByUsername")
    class ExistsByUsernameTests {

        @Test
        @DisplayName("'admin' existe → devuelve true")
        void existsByUsername_adminExiste_devuelveTrue() {
            assertThat(userRepo.existsByUsername("admin")).isTrue();
        }

        @Test
        @DisplayName("'user1' existe → devuelve true")
        void existsByUsername_user1Existe_devuelveTrue() {
            assertThat(userRepo.existsByUsername("user1")).isTrue();
        }

        @Test
        @DisplayName("Usuario inexistente → devuelve false")
        void existsByUsername_noExiste_devuelveFalse() {
            assertThat(userRepo.existsByUsername("fantasma")).isFalse();
        }
    }

    /**
     * Pruebas unitarias:
     * Username duplicado → lanza excepción de constraint
     * Username único → se guarda correctamente
     */
    @Nested
    @DisplayName("Unicidad de username")
    class UnicidadTests {

        @Test
        @DisplayName("Username duplicado → lanza excepción de constraint")
        void username_duplicado_lanzaExcepcion() {
            assertThatThrownBy(() -> {
                userRepo.saveAndFlush(new User("admin", "otroPwd", Role.USER));
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Username único → se guarda correctamente")
        void username_unico_seGuarda() {
            User nuevo = new User("usuarioUnico123", "pwd", Role.USER);
            User guardado = userRepo.saveAndFlush(nuevo);

            assertThat(guardado.getId()).isNotNull();
            assertThat(userRepo.existsByUsername("usuarioUnico123")).isTrue();
        }
    }
}
