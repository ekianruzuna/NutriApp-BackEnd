package com.NutriApp.NutriApp.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class WebCorsConfig {


    //clase para configurar el CORS que es el filtro que pasa cuando desde el front
    //se hace una peticion especial (peticion con headers o con creedenciales)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        // Spring necesita un objeto CorsConfiguration para saber qué orígenes, métodos
        // y headers aceptar durante el preflight (OPTIONS) que hace el navegador.
        CorsConfiguration config = new CorsConfiguration();

        // Frontend permitido
        config.setAllowedOrigins(List.of("http://localhost:4200", "https://dreamy-figolla-211a03.netlify.app"));

        // Métodos permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Headers permitidos (incluye Authorization)
        config.setAllowedHeaders(List.of("*"));

        // Necesario si vas a enviar cookies/tokens
        config.setAllowCredentials(true);

        // Registrar config en todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
