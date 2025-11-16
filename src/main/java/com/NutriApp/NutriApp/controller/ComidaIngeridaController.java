package com.NutriApp.NutriApp.controller;

import com.NutriApp.NutriApp.modelo.dto.ComidaIngeridaDTO;
import com.NutriApp.NutriApp.modelo.dto.MensajeDTO;
import com.NutriApp.NutriApp.modelo.dto.ModificarComidaIngeridaDTO;
import com.NutriApp.NutriApp.exceptions.DiaInvalidoException;
import com.NutriApp.NutriApp.modelo.ComidaIngerida;
import com.NutriApp.NutriApp.modelo.Dia;
import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.enums.TipoComida;
import com.NutriApp.NutriApp.service.ComidaIngeridaService;
import com.NutriApp.NutriApp.service.DiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Validated
@RestController
@RequestMapping("/comidas")  // prefijo de la ruta
@RequiredArgsConstructor
@Tag(name = "Comidas Ingeridas", description = "Operaciones con las comidas ingeridas")
public class ComidaIngeridaController {

    private final ComidaIngeridaService comidaIngeridaService;
    private final DiaService diaService;

    @Operation(summary = "Agregar alimento ingerido.", description = "Agrega un alimento ingerido a la BDD.")
    @PostMapping("/agregar")
    public ResponseEntity<Map<String, String>> agregarComidaIngerida(@Valid @RequestBody ComidaIngeridaDTO comidaIngeridaDTO) throws Exception {
        comidaIngeridaService.agregarComidaIngerida(
                comidaIngeridaDTO.getId(),
                comidaIngeridaDTO.getNombreComida(),
                comidaIngeridaDTO.getGramos(),
                comidaIngeridaDTO.getTipoComida(),
                comidaIngeridaDTO.getFecha()
        );

        return ResponseEntity.ok(
                Map.of("mensaje", "Se cargó correctamente el alimento " + comidaIngeridaDTO.getNombreComida())
        );
    }

    @PutMapping("/modificar")
    public ResponseEntity<MensajeDTO> modificarComidaIngerida(
            @RequestBody ModificarComidaIngeridaDTO dto) throws Exception {

        comidaIngeridaService.modificarComida(dto);

        return ResponseEntity.ok(new MensajeDTO("Comida modificada correctamente"));
    }


    @Operation(summary = "Ver calorias consumidas de un dia.", description = "Devuelve las calorias consumidas en un dia.")
    @GetMapping("/calorias-dia")
    public ResponseEntity<Double> verCaloriasConsumidasDeunDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        double totalCalorias = comidaIngeridaService.verCaloriasConsumidasDeunDia(fecha);
        return ResponseEntity.ok(totalCalorias);
    }



    @Operation(summary = "Eliminar una comida ingerida.", description = "Elimina una comida ingerida en un dia.")
    @DeleteMapping("/eliminar")
    public ResponseEntity<Map<String, String>> eliminarComida(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam long comidaId,
            @RequestParam String tipoComida) {

        TipoComida tipoEnum;
        try {
            tipoEnum = TipoComida.valueOf(tipoComida.toUpperCase()); // Convierte String a Enum
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Tipo de comida inválido"));
        }

        comidaIngeridaService.eliminarComidaIngerida(fecha, comidaId, tipoEnum);

        return ResponseEntity.ok(Map.of("mensaje", "La comida se eliminó correctamente."));
    }
}

