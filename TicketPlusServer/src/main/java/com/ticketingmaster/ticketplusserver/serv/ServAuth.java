package com.ticketingmaster.ticketplusserver.serv;

import org.springframework.stereotype.Service;
import com.ticketingmaster.ticketplusserver.model.User;
import com.ticketingmaster.ticketplusserver.repo.UserRepo;
import java.util.Optional;

/**
 * Clase que conforma el servei d'autenticació, consta d'un constructor
 * que rep cualsevol clase que implementi la interficie UsuariRepo. Si
 * el nom d'usuari no conincideix amb ningun aquesta retorna false, i sino
 * compara les contraseñes per donar una resposta valida o no al tipus de LOGIN.
 * @author David
 */
@Service
public class ServAuth {

    private final UserRepo userRepository;

    public ServAuth(UserRepo userRepository) {
        this.userRepository = userRepository;
    }

    public boolean login(String username, String password) {

        Optional<User> user = userRepository.findByUsername(username);

        if (user == null) {
            return false;
        }

        return user.get().getPassword().equals(password);
    }
}