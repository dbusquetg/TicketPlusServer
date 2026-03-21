/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ticketingmaster.ticketplusserver.serv;

import com.ticketingmaster.ticketplusserver.model.Role;
import com.ticketingmaster.ticketplusserver.model.User;
import com.ticketingmaster.ticketplusserver.repo.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 *
 * @author Christian
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepo userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Solo crea los usuarios si no existen ya en la BBDD
        if (!userRepository.existsByName("admin")) {
            User admin = new User(
                "admin",
                passwordEncoder.encode("admin123"),  // Hash generado por Spring
                Role.ADMIN
            );
            userRepository.save(admin);
            System.out.println("Usuario 'admin' creado.");
        }

        if (!userRepository.existsByName("user1")) {
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
