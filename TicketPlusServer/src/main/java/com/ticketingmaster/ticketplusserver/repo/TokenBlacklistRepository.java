/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ticketingmaster.ticketplusserver.repo;

import com.ticketingmaster.ticketplusserver.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
/**
 * Interface que conforma el repositorio de tokens en la lista negra.
 */
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, String> {

    boolean existsByTokenHash(String tokenHash);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
