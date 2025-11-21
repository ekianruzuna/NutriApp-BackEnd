package com.NutriApp.NutriApp.controller;

import com.NutriApp.NutriApp.service.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/verificacion")
public class EmailVerificationController {

    @Autowired
    private EmailVerificationService emailService;

    @PostMapping("/email/enviar-codigo")
    public ResponseEntity<?> enviarCodigo(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email requerido");
        }

        String codigo = emailService.generarCodigo();
        emailService.guardarCodigo(email, codigo);
        emailService.enviarCorreo(email, codigo);

        return ResponseEntity.ok(Map.of("mensaje", "Código enviado"));
    }

    @PostMapping("/email/verificar-codigo")
    public ResponseEntity<Map<String, Object>> verificarCodigo(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String codigo = body.get("codigo");

        Map<String, Object> response = new HashMap<>();

        if (email == null || codigo == null) {
            response.put("exito", false);
            response.put("mensaje", "Datos incompletos");
            return ResponseEntity.ok(response);
        }

        boolean valido = emailService.verificar(email, codigo);

        if (!valido) {
            response.put("exito", false);
            response.put("mensaje", "Código incorrecto o expirado");
            return ResponseEntity.ok(response);
        }

        emailService.eliminarCodigo(email);

        response.put("exito", true);
        response.put("mensaje", "Correo verificado correctamente");
        return ResponseEntity.ok(response);

    }
}
