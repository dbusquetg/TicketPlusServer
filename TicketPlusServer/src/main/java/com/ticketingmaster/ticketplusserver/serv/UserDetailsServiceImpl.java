package com.ticketingmaster.ticketplusserver.serv;

import com.ticketingmaster.ticketplusserver.model.User;
import com.ticketingmaster.ticketplusserver.repo.UserRepo;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de UserDetailsService usada por Spring Security
 * para cargar los datos del usuario durante la autenticación.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepo userRepository;

    public UserDetailsServiceImpl(UserRepo userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("Usuario desactivado: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
