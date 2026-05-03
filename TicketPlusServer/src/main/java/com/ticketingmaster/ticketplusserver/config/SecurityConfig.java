package com.ticketingmaster.ticketplusserver.config;

import com.ticketingmaster.ticketplusserver.security.JwtAuthFilter;
import com.ticketingmaster.ticketplusserver.serv.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
/**
 * Clase que engloba la configuración de seguridad del servicio JWT Token
 * @author David Busquet
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity           // Permite usar @PreAuthorize en los controllers
public class SecurityConfig {

    private final JwtAuthFilter          jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;
    
    /**
     * Constructor del SecurityConfig, recibe el filtro JWT y los detalles del
     * usuario en formato.
     * @param jwtAuthFilter Filtro JWT en formato jwtAuthFilter.
     * @param userDetailsService  Detalles del servicio de usuario en formato userDetalsService.
     */
    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthFilter    = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }
    
    /**
     * Recibe un HttpSecurity y devuelve la cadena de filtro de seguridad.
     * Desactiva CSRF para API Stateless, aplica reglas de acceso, implementa el
     * proveedor de autenticación BCrypt y añade el filtro JWT junto con el Spring
     * security.
     * Usa la keypass generada en el servidor.
     * @param http Seguridad HTTP en formato HttpSecutirty.
     * @return Cadena de filtro de seguridad en formato SecutiryFilterChain
     * @throws Exception General.
     */
    @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        // Fuerza todas las peticiones a usar HTTPS
        .requiresChannel(channel ->
            channel.anyRequest().requiresSecure()
        )
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/login").permitAll()
            .anyRequest().authenticated()
        )
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
    
    /**
     * Devuelve un AuthenticationProvider con los detalles del usuario y password
     * encoder.
     * @return Proveedor de autenticación en formato AuthenticationProvider.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    /**
     * Devueluve un AutenticationManager cuando se le pasa un AutenthicationConfiguration.
     * @param config Configuración de autenthicación en formato AuthenticationConfiguration.
     * @return Gestor de autenticaciones AuthenticationManager.
     * @throws Exception General.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * Devuelve un PasswordEncoder como BCrypt password encoder.
     * @return Codificador de password en formato PasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
