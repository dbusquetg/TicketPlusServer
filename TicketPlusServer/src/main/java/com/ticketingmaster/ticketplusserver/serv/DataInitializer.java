package com.ticketingmaster.ticketplusserver.serv;

import com.ticketingmaster.ticketplusserver.model.Role;
import com.ticketingmaster.ticketplusserver.model.User;
import com.ticketingmaster.ticketplusserver.repo.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 *Clase que controla el servicio de inicialización de datos, si existen
 * en la BD o no.
 * @author David Busquet
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     *  Constructor que recibe Repositorio de Usuario y un Password Encoder.
     * @param userRepository Repositorio del usuario
     * @param passwordEncoder Codificador de contraseñas
     */
    public DataInitializer(UserRepo userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Run que inicializa el Data Initializer, se pasan argumentos en forma de String.
     * @param args  Argumentos en formato String.
     */
    @Override
    public void run(String... args) {
        // Solo crea los usuarios si no existen ya en la BBDD
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User(
                "admin",
                passwordEncoder.encode("admin123"),  // Hash generado por Spring
                Role.ADMIN
            );
            userRepository.save(admin);
            System.out.println("Usuario 'admin' creado.");
        }

        if (!userRepository.existsByUsername("user1")) {
            User user = new User(
                "user1",
                passwordEncoder.encode("admin123"),
                Role.USER
            );
            userRepository.save(user);
            System.out.println("Usuario 'user1' creado.");
        }
    }
}
