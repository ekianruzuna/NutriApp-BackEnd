package com.NutriApp.NutriApp.controller;

import com.NutriApp.NutriApp.modelo.Logros.Logro;
import com.NutriApp.NutriApp.service.LogroService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@Tag(name = "Logros", description = "Operaciones con los logros")
@RequestMapping("/api/logro")
public class LogroController {

    private final LogroService logroService;

    @GetMapping("/prueba")
    public ResponseEntity<String> logroPrueba(@RequestParam LocalDate fechaComprobar){
        logroService.comprobarYGuardarMetaCaloricaDiaria(fechaComprobar);
        return ResponseEntity.ok("Ya se comprobo");
    }
}
