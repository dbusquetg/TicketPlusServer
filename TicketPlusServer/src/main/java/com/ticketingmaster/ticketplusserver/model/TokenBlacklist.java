package com.ticketingmaster.ticketplusserver.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Clase encargada de poner los tockens en la lista negra i configurarlos
 * para que expiren cuando el programa lo necesita. Consta del hash del token
 * con el que se ha hecho login y un LocalDateTime que indica cuando expirara.
 * @author David Busquet
 */
@Entity
@Table(name = "token_blacklist")
public class TokenBlacklist {

    @Id
    @Column(name = "token_hash", length = 512)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public TokenBlacklist() {}
    /**
     * Constructor de la instancia blacklist para un token.
     * @param tokenHash Hash del token.
     * @param expiresAt Tiempo cuando expirara.
     */
    public TokenBlacklist(String tokenHash, LocalDateTime expiresAt) {
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public String getTokenHash()                   { return tokenHash; }
    public void setTokenHash(String tokenHash)     { this.tokenHash = tokenHash; }

    public LocalDateTime getExpiresAt()            { return expiresAt; }
    public void setExpiresAt(LocalDateTime exp)    { this.expiresAt = exp; }
}