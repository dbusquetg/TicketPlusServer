package com.ticketingmaster.ticketplusserver.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist")
public class TokenBlacklist {

    @Id
    @Column(name = "token_hash", length = 512)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // ─── Constructors ─────────────────────────────────────────

    public TokenBlacklist() {}

    public TokenBlacklist(String tokenHash, LocalDateTime expiresAt) {
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    // ─── Getters & Setters ────────────────────────────────────

    public String getTokenHash()                   { return tokenHash; }
    public void setTokenHash(String tokenHash)     { this.tokenHash = tokenHash; }

    public LocalDateTime getExpiresAt()            { return expiresAt; }
    public void setExpiresAt(LocalDateTime exp)    { this.expiresAt = exp; }
}