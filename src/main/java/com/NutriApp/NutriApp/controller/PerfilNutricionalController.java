package com.NutriApp.NutriApp.controller;

import com.NutriApp.NutriApp.modelo.PerfilNutricional;
import com.NutriApp.NutriApp.modelo.dto.PerfilNutricionalDTO;
import com.NutriApp.NutriApp.service.PerfilNutricionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
@Validated
@RestController
@RequestMapping("/api/perfil-nutricional")
@RequiredArgsConstructor
@Tag(name = "Perfil nutricional", description = "Operaciones con el perfil nutricional")
public class PerfilNutricionalController {

    private final PerfilNutricionalService perfilNutricionalService;

    // Obtener el perfil nutricional actual del usuario autenticado
    @Operation(summary = "Ver perfil nutricional.", description = "Devuelve el perfil nutricional del usuario logeado.")
    @GetMapping("/obtener")
    public ResponseEntity<PerfilNutricional> obtenerPerfil() {
        PerfilNutricional perfil = perfilNutricionalService.obtenerPerfilNutricional();
        return ResponseEntity.ok(perfil);
    }

    @Operation(summary = "Modificar perfil nutricional.", description = "Modifica el perfil nutricional del usuario logeado.")
    @PutMapping("/actualizar")
    public ResponseEntity<PerfilNutricional> actualizarPerfil(@Valid @RequestBody PerfilNutricionalDTO perfilDTO) {
        return ResponseEntity.ok(perfilNutricionalService.actualizarPerfilNutricional(perfilDTO));
    }
}