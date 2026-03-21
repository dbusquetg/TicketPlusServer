package com.ticketingmaster.ticketplusserver.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ticketingmaster.ticketplusserver.model.User;
import java.util.Optional;
/**
 * Interfaz de la clase Usuario para el repositorio. Implementa la función
 * findByUsername para poder buscar usuarios según su clave primaria, y una 
 * función para comprobar si el usuario existe.
 */
public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    
}