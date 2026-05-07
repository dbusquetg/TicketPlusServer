package com.ticketingmaster.ticketplusserver.serv;

import com.ticketingmaster.ticketplusserver.dto.AgentResponse;
import com.ticketingmaster.ticketplusserver.dto.CreateUserRequest;
import com.ticketingmaster.ticketplusserver.dto.UserResponse;
import com.ticketingmaster.ticketplusserver.model.Role;
import com.ticketingmaster.ticketplusserver.model.User;
import com.ticketingmaster.ticketplusserver.repo.UserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de usuarios.
 * @author David Busquet
 */
@Service
public class ServUser {

    private final UserRepo        userRepo;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Constructor que admite un repositorio de usuarios y el password encoder.
     * @param userRepo
     * @param passwordEncoder 
     */
    public ServUser(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo        = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Devuelve todos los usuarios activos ordenados por username.
     * Nunca expone passwordHash.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> listarTodos() {
        return userRepo.findByActiveTrueOrderByUsernameAsc().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve todos los usuarios con rol ADMIN.
     * Se usa para cargar dinámicamente el listado de agentes
     * disponibles en el cliente (TicketDetailPanel).
     *
     * Nunca expone passwordHash ni active — solo id, username y role.
     *
     * @return lista de AgentResponse con los agentes del sistema.
     */
    @Transactional(readOnly = true)
    public List<AgentResponse> listarAgentes() {
        return userRepo.findByRoleAndActiveTrueOrderByUsernameAsc(Role.ADMIN).stream()
                .map(AgentResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Crea un usuario nuevo o reactiva uno que estaba desactivado.
     * Si el username ya existe y está activo lanza excepción.
     * Si el username existía pero estaba desactivado (active=false), lo reactiva.
     *
     * @param request datos del nuevo usuario.
     * @return UserResponse con los datos del usuario creado o reactivado.
     */
    @Transactional
    public UserResponse crear(CreateUserRequest request) {
        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol no válido: " + request.getRole() +
                    ". Valores aceptados: USER, ADMIN");
        }

        // Si el usuario existía pero estaba desactivado, lo reactivamos
        if (userRepo.existsByUsername(request.getUsername())) {
            User existente = userRepo.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Error al buscar usuario"));

            if (existente.isActive()) {
                throw new IllegalArgumentException("El username '" + request.getUsername() + "' ya está en uso");
            }

            existente.setActive(true);
            existente.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            existente.setRole(role);
            return UserResponse.from(userRepo.save(existente));
        }

        User nuevo = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                role
        );
        return UserResponse.from(userRepo.save(nuevo));
    }

    /**
     * Desactiva un usuario marcándolo como active=false.
     * No borra físicamente de la BD para preservar tickets y comentarios.
     *
     * Restricciones:
     * No puedes eliminarte a ti mismo.
     * No puedes eliminar el último administrador activo.
     *
     * @param username        username del usuario a desactivar.
     * @param usuarioActual   username del usuario autenticado que hace la petición.
     */
    @Transactional
    public void eliminar(String username, String usuarioActual) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

        if (!user.isActive()) {
            throw new RuntimeException("El usuario ya estaba desactivado: " + username);
        }

        if (username.equals(usuarioActual)) {
            throw new IllegalArgumentException("No puedes eliminarte a ti mismo");
        }

        if (user.getRole() == Role.ADMIN) {
            long adminsActivos = userRepo.countByRoleAndActiveTrue(Role.ADMIN);
            if (adminsActivos <= 1) {
                throw new IllegalArgumentException("No puedes eliminar el último administrador activo");
            }
        }

        user.setActive(false);
        userRepo.save(user);
    }
}