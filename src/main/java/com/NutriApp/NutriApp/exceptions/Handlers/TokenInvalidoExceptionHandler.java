package com.NutriApp.NutriApp.exceptions.Handlers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TokenInvalidoExceptionHandler implements AuthenticationEntryPoint {    //clase para manejar los mensajes de error de perimosos insuficientes

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        final String expiredMessage = (String) request.getAttribute("expired");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        if (expiredMessage != null){
            response.getWriter().write("Token expirado = " + expiredMessage);
        }else {
            response.getWriter().write("No estás autenticado o el token es inválido");
        }

    }
}
