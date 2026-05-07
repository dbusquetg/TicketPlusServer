package com.ticketingmaster.ticketplusserver.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ticketingmaster.ticketplusserver.model.User;
import com.ticketingmaster.ticketplusserver.model.Role;
import java.util.List;
import java.util.Optional;
/**
 * Interfaz de la clase Usuario para el repositorio. Implementa la función
 * findByUsername para poder buscar usuarios según su clave primaria, y una 
 * función para comprobar si el usuario existe.
 * @author David Busquet
 */
public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    /**
     * Devuelve todos los usuarios con un rol concreto.
     *
     * @param role rol solicitado
     * @return lista de usuarios con ese rol
     */
    List<User> findByRole(Role role);

    /**
     * Devuelve todos los usuarios activos ordenados por username.
     *
     * @return lista de usuarios activos
     */
    List<User> findByActiveTrueOrderByUsernameAsc();

    /**
     * Devuelve todos los usuarios activos de un rol concreto ordenados por username.
     *
     * @param role rol solicitado
     * @return lista de usuarios activos con ese rol
     */
    List<User> findByRoleAndActiveTrueOrderByUsernameAsc(Role role);

    /**
     * Cuenta cuántos usuarios activos hay con un rol concreto.
     *
     * @param role rol solicitado
     * @return número de usuarios activos con ese rol
     */
    long countByRoleAndActiveTrue(Role role);
}