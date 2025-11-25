package com.NutriApp.NutriApp.config;

import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.service.JwtService;
import com.NutriApp.NutriApp.service.UsuarioService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Obtenemos el header "Authorization" del request
        final String authHeader = request.getHeader("Authorization");

        // Verificamos si el header es nulo o no empieza con "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // No hay token, dejamos que el resto de la cadena siga
        }

        // Extraemos el token sin el prefijo "Bearer "
        String token = authHeader.substring(7);

        try {
            // Obtenemos el nombre de usuario desde el token
            String username = jwtService.extractUsername(token);

            // Si obtenemos un username y no hay autenticación en contexto, validamos el token
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Cargamos los datos del usuario desde base de datos
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Verificamos si el token es válido
                if (jwtService.isTokenValid(token, userDetails)) {

                    // Creamos el token de autenticación de Spring
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Cargamos información adicional del request
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Establecemos el usuario autenticado en el contexto de Spring
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Logica para actualizar la fecha activa del usuario
                    Usuario usuario = userDetailsService.buscarPorEmail(userDetails.getUsername());
                    if (usuario != null) {
                        LocalDate hoy = LocalDate.now();
                        if (usuario.getFechaActiva() == null || !usuario.getFechaActiva().equals(hoy)) {
                            usuario.setFechaActiva(hoy);
                            userDetailsService.guardar(usuario);
                        }
                    }


                }
            }


        }catch (ExpiredJwtException ex){
//            request.setAttribute("expired", ex.getMessage());

            //lanzamos la exception para que corte el flujo ya que sino (como estaba antes), el flujo seguia y lleguaba al
            //controller donde accedia a los metodos, y si se intentaba acceder al usuario del contexto
            //de spring tiraba un error que no se manejaba porque no habia ningun usuario en el contexto de spring.
            //Ahora esta exception se catpura en exceptionHandling, en autenticationEntryPoint (es donde se
            //capturan todas las exception de autenticacion) en el filterChain de securityConfig y se corta la ejecucion
            //entonce no llega a los metodos de los controllers
            throw new BadCredentialsException("Token expirado: ", ex);
        }

        // Continuamos la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
