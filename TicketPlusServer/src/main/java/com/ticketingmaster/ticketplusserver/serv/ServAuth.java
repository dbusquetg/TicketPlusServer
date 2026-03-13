package com.ticketingmaster.ticketplusserver.serv;

import org.springframework.stereotype.Service;
import com.ticketingmaster.ticketplusserver.model.Usuari;
import com.ticketingmaster.ticketplusserver.repo.UsuariRepo;

@Service
public class ServAuth {

    private final UsuariRepo userRepository;

    public ServAuth(UsuariRepo userRepository) {
        this.userRepository = userRepository;
    }

    public boolean login(String username, String password) {

        Usuari user = userRepository.findByUsername(username);

        if (user == null) {
            return false;
        }

        return user.getContrasenya().equals(password);
    }
}