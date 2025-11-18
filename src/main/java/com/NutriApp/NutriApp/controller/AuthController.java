package com.NutriApp.NutriApp.controller;

import com.NutriApp.NutriApp.exceptions.UsuarioInexistenteException;
import com.NutriApp.NutriApp.modelo.dto.LoginRequest;
import com.NutriApp.NutriApp.modelo.dto.LoginResponse;
import com.NutriApp.NutriApp.modelo.dto.RegistroUsuarioRequest;
import com.NutriApp.NutriApp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
@Validated
@RestController // Define que esta clase manejará peticiones HTTP
@RequestMapping("/auth") // El endpoint completo será /auth/login
@Tag(name = "Autorizacion", description = "Operaciones de autorizacion")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "Logearse.", description = "Devuelve un token del usuario logeado.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse res = authService.login(request);
            return ResponseEntity.ok(res);
        } catch (UsuarioInexistenteException | BadCredentialsException e) {
            // Usuario o contraseña incorrectos
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            // Otro error inesperado
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    @Operation(summary = "Registrarse.", description = "Inserta un nuevo usuario, persona, y perfil nutricional en nustra BDD.")
    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registrarUsuario(@Valid @RequestBody RegistroUsuarioRequest request) {
        return ResponseEntity.ok(authService.registrarUsuario(request));
    }


}