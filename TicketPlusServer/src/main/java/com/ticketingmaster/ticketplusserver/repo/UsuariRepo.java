package com.ticketingmaster.ticketplusserver.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ticketingmaster.ticketplusserver.model.Usuari;

public interface UsuariRepo extends JpaRepository<Usuari, Long> {

    Usuari findByUsername(String nomusuari);

}