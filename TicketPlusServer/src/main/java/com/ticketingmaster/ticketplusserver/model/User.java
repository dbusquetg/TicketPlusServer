
package com.ticketingmaster.ticketplusserver.model;

import jakarta.persistence.*;
/**
 * Clase Usuari que conforma l'entitat usuari per fer LOGIN, de moment
 * nomes conforma les dades nom del usuari i contrasenya, pero mes 
 * endavant sera suceptible de canvi.
 * @author David
 */
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;
    
    @Column(nullable = false)
    private boolean active = true;
    
    public User() {}
    
     // ─── Constructors ─────────────────────────────────────────
    public User(String username, String passwordHash, Role role) {
        this.username     = username;
        this.passwordHash = passwordHash;
        this.role         = role;
    }

    // ─── Getters & Setters ────────────────────────────────────

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public String getUsername()                { return username; }
    public void setUsername(String username)   { this.username = username; }

    public String getPasswordHash()            { return passwordHash; }
    public void setPasswordHash(String hash)   { this.passwordHash = hash; }

    public Role getRole()                      { return role; }
    public void setRole(Role role)             { this.role = role; }

    public boolean isActive()                  { return active; }
    public void setActive(boolean active)      { this.active = active; }
    
}