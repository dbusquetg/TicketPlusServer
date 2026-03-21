package com.ticketingmaster.ticketplusserver.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ticketingmaster.ticketplusserver.model.User;
import java.util.Optional;
/**
 * Interficie de la clase Usuari per al repositori. Implementa la funció
 * buscaPerNomUsuari per poder buscar usuaris segons la seva clau primari.
 * @author David
 */
public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);
    boolean existsByName(String name);
    Optional<User> findById(String id);

}