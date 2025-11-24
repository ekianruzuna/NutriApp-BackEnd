package com.NutriApp.NutriApp.config;

import com.NutriApp.NutriApp.exceptions.Handlers.AccesDeniedExceptionHandler;
import com.NutriApp.NutriApp.exceptions.Handlers.TokenInvalidoExceptionHandler;
import com.NutriApp.NutriApp.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {


    /* dejo comentado por si no tenemos acceso a la base de datos en algun momento
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }*/

    @Autowired
    private AccesDeniedExceptionHandler accesDeniedExceptionHandler;

    @Autowired
    private TokenInvalidoExceptionHandler tokenInvalidoExceptionHandler;

    // Configuración del filtro de seguridad para proteger rutas y validar JWT
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthFilter,
                                                   CorsConfigurationSource corsConfigurationSource,
                                                   UsuarioService userDetailsService) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())

                // Activamos CORS dentro de Spring Security y le mandamos el CorsConfig previamente configurado
                // porque su propio filtro intercepta las solicitudes que llegan con headers
                // especiales (headers, credenciales, etc), y si no lo configuramos acá,
                // las peticiones desde Angular serán bloqueadas.
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/usuario/registro").permitAll() // <- Permitir acceso sin login
                        .requestMatchers("api/alimentos/buscar").permitAll()
                        .requestMatchers("api/alimentos/detalle/{fdcId}").permitAll()
                        .requestMatchers("/auth/login", "/auth/registro").permitAll()

                        //roles
                        .requestMatchers("api/rol/cambiar/cliente").hasRole("ADMIN")
                        .requestMatchers("api/rol/cambiar/admin").hasRole("ADMIN")

                        //personas
                        .requestMatchers("/api/persona/guardar").hasRole("ADMIN")
                        .requestMatchers("/api/persona/listar").hasRole("ADMIN")

                        //swagger
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        //http://localhost:8080/swagger-ui/index.html#/ -> direccion para entrar a la documentacion de swagger

                        //solicitudes
                        .requestMatchers("/api/solicitud/listar/todas").hasRole("ADMIN")
                        .requestMatchers("/api/solicitud/filtrar/fecha").hasRole("ADMIN")
                        .requestMatchers("/api/solicitud/filtrar/username").hasRole("ADMIN")
                        .requestMatchers("/api/solicitud/filtrar/nombreComida").hasRole("ADMIN")
                        .requestMatchers("/api/persona/obtener").hasRole("ADMIN")
                        .requestMatchers("/api/solicitud/aceptar").hasRole("ADMIN")
                        .requestMatchers("/api/solicitud/rechazar").hasRole("ADMIN")
                        .requestMatchers("api/solicitud/modificarAndAceptar").hasRole("ADMIN")

                        //alimentos ingresados por el usuario
                        .requestMatchers("api/alimentos-usuario/listarTodos").hasRole("ADMIN")
                        .requestMatchers("api/alimentos-usuario/filtrar").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accesDeniedExceptionHandler)
                        //maneja la exception de acceso denegado aca
                        //porque antes que llegue a globalExceptionHandler
                        //sea catchea antes entonces hay que manejarla aca

                        .authenticationEntryPoint(tokenInvalidoExceptionHandler) //manejador para token faltante o invalido
                )

                .authenticationProvider(authenticationProvider(userDetailsService))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }

    // Proveedor de autenticación que conecta al servicio de usuarios y al codificador
    @Bean
    public AuthenticationProvider authenticationProvider(UsuarioService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // AuthenticationManager usando directamente el AuthenticationProvider
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }

    //Eleccion del tipo de passwordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Por ejemplo, BCryptPasswordEncoder es una buena práctica
        return new BCryptPasswordEncoder();
    }


    // nos permite manejar usuarios desde el codigo Java
    @Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    //herencia de roles
    @Bean
    public RoleHierarchy roleHierarchy() {
        var hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(
                "ROLE_BOSS > ROLE_CLIENT"
        );
        return hierarchy;
    }
}

