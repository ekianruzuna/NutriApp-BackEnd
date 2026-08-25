package com.NutriApp.NutriApp.controller;

import com.NutriApp.NutriApp.exceptions.UsuarioInexistenteException;

import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.dto.LoginRequest;
import com.NutriApp.NutriApp.modelo.dto.LoginResponse;
import com.NutriApp.NutriApp.modelo.dto.RegistroUsuarioRequest;
import com.NutriApp.NutriApp.repository.EmailVerificationRepository;
import com.NutriApp.NutriApp.repository.UsuarioRepository;
import com.NutriApp.NutriApp.service.AuthService;
import com.NutriApp.NutriApp.service.JwtService;
import com.NutriApp.NutriApp.service.UsuarioValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
@Validated
@RestController // Define que esta clase manejará peticiones HTTP
@RequestMapping("/auth") // El endpoint completo será /auth/login
@Tag(name = "Autorizacion", description = "Operaciones de autorizacion")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;

    private final UsuarioValidationService validationService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

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

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        String idTokenString = body.get("token");

        if (idTokenString == null || idTokenString.isBlank()) {
            return ResponseEntity.badRequest().body("Token no recibido");
        }

        try {
            // AHORA el servicio te devuelve un mapa o DTO simple,
            // sin depender de librerías de Google en el controlador.
            Map<String, Object> userData = authService.verifyGoogleToken(idTokenString);

            if (userData == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
            }

            String email = (String) userData.get("email");

            // Buscar usuario en DB (esto sigue igual)
            Optional<Usuario> usuarioOpt = usuarioRepository.findByPersonaEmail(email);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                String jwt = jwtService.generateToken(usuario);
                Map<String, Object> response = new HashMap<>();
                response.put("registered", true);
                response.put("token", jwt);
                response.put("email", email);
                response.put("username", usuario.getUsername());
                return ResponseEntity.ok(response);
            } else {
                // Usuario no registrado
                return ResponseEntity.ok(userData); // Devuelve los datos extraídos para el frontend
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error verificando token: " + e.getMessage());
        }
    }




    @Operation(summary = "Registrarse.", description = "Inserta un nuevo usuario, persona, y perfil nutricional en nustra BDD.")
    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registrarUsuario(@Valid @RequestBody RegistroUsuarioRequest request) {
        return ResponseEntity.ok(authService.registrarUsuario(request));
    }

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(Map.of("disponible", validationService.usernameDisponible(username)));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(Map.of("disponible", validationService.emailDisponible(email)));
    }

    @GetMapping("/check-dni")
    public ResponseEntity<Map<String, Boolean>> checkDni(@RequestParam String dni) {
        return ResponseEntity.ok(Map.of("disponible", validationService.dniDisponible(dni)));
    }

    @GetMapping("/check-telefono")
    public ResponseEntity<Map<String, Boolean>> checkTelefono(@RequestParam String telefono) {
        return ResponseEntity.ok(Map.of("disponible", validationService.telefonoDisponible(telefono)));
    }


}