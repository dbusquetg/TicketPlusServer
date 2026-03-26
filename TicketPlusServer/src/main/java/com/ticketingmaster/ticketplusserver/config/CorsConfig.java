package com.ticketingmaster.ticketplusserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.List;

/**
 * Configuración CORS para permitir peticiones desde el cliente Swing
 * (y cualquier otro origen durante desarrollo).
 * En producción, reemplaza allowedOriginPatterns("*") con dominios concretos.
 * @author David Busquet
 */
@Configuration
public class CorsConfig {
    /**
     * Constructor corsfilter que configura y devuelve un objeto CorsFilter para
     * las funciones permitidas y las cabeceras permitidas, asi como credenciales
     * y patrones.
     * @return un objeto CorsFilter.
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
