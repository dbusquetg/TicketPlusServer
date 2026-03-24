package com.ticketingmaster.ticketplusserver.serv.task;

import com.ticketingmaster.ticketplusserver.repo.TokenBlacklistRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Tarea programada que limpia automáticamente los tokens
 * expirados de la blacklist para evitar que la tabla crezca
 * indefinidamente.
 *@author David Busquet
 * 
 * Se ejecuta cada hora (3 600 000 ms).
 * Requiere @EnableScheduling en ServerApplication.
 */
@Component
public class TokenCleanupTask {

    private final TokenBlacklistRepository blacklistRepository;

    public TokenCleanupTask(TokenBlacklistRepository blacklistRepository) {
        this.blacklistRepository = blacklistRepository;
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void purgeExpiredTokens() {
        blacklistRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
