package com.ticketingmaster.ticketplusserver.security;

import com.ticketingmaster.ticketplusserver.repo.TokenBlacklistRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que se ejecuta una vez por request.
 * Extrae el JWT del header Authorization, lo valida,
 * comprueba que no esté en la blacklist y establece
 * la autenticación en el SecurityContext.
 * @author David Busquet
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistRepository blacklistRepository;
    /**
     * Constructor que consta de jwUtil y blacklist repository.
     * @param jwtUtil Instancia utilitaria de jwtUtil
     * @param blacklistRepository Repositorio de la blacklist.
     */
    public JwtAuthFilter(JwtUtil jwtUtil,
                         TokenBlacklistRepository blacklistRepository) {
        this.jwtUtil             = jwtUtil;
        this.blacklistRepository = blacklistRepository;
    }
    
    /**
     * Función que ejecuta el filtro interno para el logout y gestiona
     * la blacklist.
     * @param request Solcitud en forma de HttpServletRequest
     * @param response Respuesta en forma de HttpServletResponse
     * @param filterChain Filtro de cadena
     * @throws ServletException Excepción de Servlet
     * @throws IOException Excepcion de IOE
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Si no hay header o no empieza con "Bearer ", continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // Rechazar tokens en la blacklist (logout)
        if (blacklistRepository.existsByTokenHash(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validar JWT y establecer autenticación
        if (jwtUtil.isTokenValid(token)) {
            String username = jwtUtil.extractUsername(token);
            String role     = jwtUtil.extractRole(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
