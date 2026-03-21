/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
     * @param jwtAuthFilter
     * @param userDetailsService 
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
     * @param HttpSecurity http
     * @return SecurityFilterChain
     * @throws Exception 
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Desactivar CSRF (API stateless con JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Sin sesión HTTP — cada request se autentica por JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Reglas de acceso
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()  // Login público
                        .anyRequest().authenticated()                    // Todo lo demás requiere JWT
                )

                // Proveedor de autenticación (BCrypt + UserDetailsService)
                .authenticationProvider(authenticationProvider())

                // Añadir el filtro JWT antes del filtro de Spring Security
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    /**
     * Devuelve un AuthenticationProvider con los detalles del usuario y password
     * encoder.
     * @return AuthenticationProvider
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
     * @param config
     * @return AuthenticationManager
     * @throws Exception 
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * Devuelve un PasswordEncoder como BCrypt password encoder.
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
