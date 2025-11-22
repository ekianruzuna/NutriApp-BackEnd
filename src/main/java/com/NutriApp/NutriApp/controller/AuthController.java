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
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
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


        System.out.println("BODY RECIBIDO: " + body);

// Obtener token
        String idTokenString = body.get("token");
        System.out.println("TOKEN RECIBIDO: " + idTokenString);

        if (idTokenString == null || idTokenString.isBlank()) {
            System.out.println("ERROR: Token no recibido o vacío");
            return ResponseEntity.badRequest().body("Token no recibido");
        }

        try {
            // Verificar token
            GoogleIdToken.Payload payload = authService.verifyToken(idTokenString);
            if (payload == null) {
                System.out.println("ERROR: Token inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
            }

            // Extraer datos
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            System.out.println("Payload válido, email: " + email + ", name: " + name);

            // Buscar usuario en DB
            Optional<Usuario> usuarioOpt = usuarioRepository.findByPersonaEmail(email);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                String jwt = jwtService.generateToken(usuario);
                System.out.println("Usuario existe, generando JWT: " + jwt);

                Map<String, Object> response = new HashMap<>();
                response.put("registered", true);
                response.put("token", jwt);
                response.put("email", email);
                response.put("username", usuario.getUsername());

                return ResponseEntity.ok(response);
            } else {
                System.out.println("Usuario NO existe, enviando datos para registro");
                Map<String, Object> response = new HashMap<>();
                response.put("registered", false);
                response.put("email", email);
                response.put("name", name);
                response.put("picture", payload.get("picture"));
                response.put("locale", payload.get("locale"));

                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            System.out.println("EXCEPCION: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error verificando token: " + e.getMessage());
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