package com.ticketingmaster.ticketplusserver.serv;

import com.ticketingmaster.ticketplusserver.dto.AgentResponse;
import com.ticketingmaster.ticketplusserver.model.Role;
import com.ticketingmaster.ticketplusserver.repo.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
import java.util.stream.Collectors;
 
/**
 * Servicio de gestión de usuarios.
 * @author David.Busquet
 */
@Service
public class ServUser {
 
    private final UserRepo userRepo;
 
    public ServUser(UserRepo userRepo) {
        this.userRepo = userRepo;
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
        return userRepo.findByRole(Role.ADMIN).stream()
                .map(AgentResponse::from)
                .collect(Collectors.toList());
    }
}