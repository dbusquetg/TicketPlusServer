package com.ticketingmaster.ticketplusserver.serv;

import org.springframework.stereotype.Service;
import com.ticketingmaster.ticketplusserver.model.Usuari;
import com.ticketingmaster.ticketplusserver.repo.UsuariRepo;

/**
 * Clase que conforma el servei d'autenticació, consta d'un constructor
 * que rep cualsevol clase que implementi la interficie UsuariRepo. Si
 * el nom d'usuari no conincideix amb ningun aquesta retorna false, i sino
 * compara les contraseñes per donar una resposta valida o no al tipus de LOGIN.
 * @author David
 */
@Service
public class ServAuth {

    private final UsuariRepo userRepository;

    public ServAuth(UsuariRepo userRepository) {
        this.userRepository = userRepository;
    }

    public boolean login(String username, String password) {

        Usuari user = userRepository.buscaPerNomUsuari(username);

        if (user == null) {
            return false;
        }

        return user.getContrasenya().equals(password);
    }
}