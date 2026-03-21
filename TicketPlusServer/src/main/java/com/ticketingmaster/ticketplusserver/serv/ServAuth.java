package com.ticketingmaster.ticketplusserver.serv;

import com.ticketingmaster.ticketplusserver.dto.LoginRequest;
import com.ticketingmaster.ticketplusserver.dto.LoginResponse;
import com.ticketingmaster.ticketplusserver.model.TokenBlacklist;
import org.springframework.stereotype.Service;
import com.ticketingmaster.ticketplusserver.model.User;
import com.ticketingmaster.ticketplusserver.repo.TokenBlacklistRepository;
import com.ticketingmaster.ticketplusserver.repo.UserRepo;
import com.ticketingmaster.ticketplusserver.security.JwtUtil;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * TODO completar informacion de la clase.
 * 
 * @author David
 */
@Service
public class ServAuth {

    private final UserRepo userRepository;
    private final TokenBlacklistRepository blacklistRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public ServAuth(UserRepo userRepository, TokenBlacklistRepository blacklistRepository,
            JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.blacklistRepository = blacklistRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public LoginResponse login(LoginRequest request) {
        
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        
        User usr = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        String token = jwtUtil.generateToken(usr);
        
        return new LoginResponse(token, usr.getRole().name(), usr.getUsername());
        
    }
    
     public void logout(String bearerToken) {

        if(bearerToken == null || !bearerToken.startsWith("brear ")){
            return;
        }
        
        String token = bearerToken.substring(7);
        
        if(jwtUtil.isTokenValid(token)){
            LocalDateTime expiresAt = jwtUtil.extractExpiration(token)
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            
            blacklistRepository.save(new TokenBlacklist(token, expiresAt));
        }
    }
}