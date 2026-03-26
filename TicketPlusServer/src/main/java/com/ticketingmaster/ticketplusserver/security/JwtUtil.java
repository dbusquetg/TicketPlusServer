package com.ticketingmaster.ticketplusserver.security;

import com.ticketingmaster.ticketplusserver.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
/**
 * Clase polivalente que controla funciones varias derivadas de Jwt, tanto generación de 
 * llaves, como generación de tokens, extracción de peticiones, nombres de
 * usuario, roles, y validación de tokens.
 * @author David Busquet
 */
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiration;

    /**
     * Obtención de la clave secreta
     * @return Clave secreta en formato SecretKey.
     */
    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generación de tokens por usuario.
     * @param user Usuario como parametro de entrada.
     * @return String del token.
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    /**
     * Extracción de todas la solicitudes.
     * @param token Token de usuario
     * @return Solcitudes en formato Claims.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Extracción del nombre de usuario del token.
     * @param token Token de usuario
     * @return Nombre de usuario en formato String.
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }
    /**
     * Extración del rol del token. 
     * @param token Token de usuario
     * @return Nombre de usuario en formato String.
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
    
    /**
     * Extracción de la expiración del token
     * @param token Token de usuario
     * @return Fecha de expiración en formato Date.
     */
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * Comprobación de validación del Token.
     * @param token Token de usuario
     * @return Booleana true o false si es valido o no, respectivamente.
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
