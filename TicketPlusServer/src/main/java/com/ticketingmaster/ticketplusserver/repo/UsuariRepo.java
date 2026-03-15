package com.ticketingmaster.ticketplusserver.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ticketingmaster.ticketplusserver.model.Usuari;
/**
 * Interficie de la clase Usuari per al repositori. Implementa la funció
 * buscaPerNomUsuari per poder buscar usuaris segons la seva clau primari.
 * @author David
 */
public interface UsuariRepo extends JpaRepository<Usuari, Long> {

    Usuari buscaPerNomUsuari(String nomusuari);

}