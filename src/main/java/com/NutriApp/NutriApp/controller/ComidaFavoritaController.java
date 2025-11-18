package com.NutriApp.NutriApp.controller;

import com.NutriApp.NutriApp.modelo.dto.ComidaFavoritaDTO;
import com.NutriApp.NutriApp.modelo.dto.ComidaFavoritaSalidaDTO;
import com.NutriApp.NutriApp.modelo.dto.ComidaIngeridaDTO;
import com.NutriApp.NutriApp.modelo.dto.ModificarCantidadComidaFavoritaDTO;
import com.NutriApp.NutriApp.modelo.ComidaFavorita;
import com.NutriApp.NutriApp.modelo.enums.TipoComida;
import com.NutriApp.NutriApp.service.ComidaFavoritaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/comidas-favoritas")
@RequiredArgsConstructor
@Tag(name = "Comidas Favoritas", description = "Operaciones con las comidas favoritas")
public class ComidaFavoritaController {

    private final ComidaFavoritaService comidaFavoritaService;

    // Agregar comida favorita o actualizar cantidad si ya existe
    @Operation(summary = "Agregar alimento a una comida favorita.", description = "Agrega un alimento a un paquete de comidas favoritas.")
    @PostMapping("/agregar")
    public ResponseEntity<String> agregarComidaFavorita(@Valid @RequestBody ComidaFavoritaDTO request) {
        try {
            comidaFavoritaService.agregarComidaFavorita(request
            );
            return ResponseEntity.ok("Comida favorita agregada/modificada correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    // Modificar solo la cantidad
    @Operation(summary = "Modificar cantidad de un alimento de una comida favorita.", description = "Modifica la cantidad de un alimento de un paquete de comidas favoritas.")
    @PutMapping("/modificar-cantidad")
    public ResponseEntity<String> modificarCantidad(@Valid @RequestBody ModificarCantidadComidaFavoritaDTO dto) {
        try {
            comidaFavoritaService.modificarCantidadComidaFavorita(dto);
            return ResponseEntity.ok("Cantidad modificada correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }

    // 🔹 Listar todas las comidas favoritas de un usuario
    @GetMapping("/usuario")
    public ResponseEntity<List<ComidaFavoritaSalidaDTO>> listarPorUsuario() {
        List<ComidaFavoritaSalidaDTO> lista = comidaFavoritaService.listarComidasFavoritasPorUsuario();
        return ResponseEntity.ok(lista);
    }



    // Eliminar comida favorita por paquete y comidaId
    @Operation(summary = "Eliminar alimento de una comida favorita.", description = "Elimina un alimento de un paquete de comidas favoritas.")
    @DeleteMapping("/eliminar")
    public ResponseEntity<String> eliminarComidaFavorita(@RequestParam String nombrePaquete,
                                                         @RequestParam long comidaId) {
        try {
            comidaFavoritaService.eliminarComidaFavorita(nombrePaquete, comidaId);
            return ResponseEntity.ok("Comida favorita eliminada correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Listar alimentos de una comida favorita.", description = "Devuelve una lista de alimentos de un paquete de comidas favoritas.")
    @GetMapping("/listar")
    public ResponseEntity<?> listarPorPaquete(@RequestParam String nombrePaquete) {
        List<ComidaFavorita> favoritas = comidaFavoritaService.listarComidasFavoritasPorPaquete(nombrePaquete);

        if (favoritas.isEmpty()) {
            return ResponseEntity.ok("El paquete '" + nombrePaquete + "' no tiene comidas favoritas.");
        }

        return ResponseEntity.ok(favoritas);
    }

    @PostMapping(value = "/agregar-paquete-a-dia", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,String>> agregarPaqueteADia(
            @RequestParam String nombrePaquete,
            @RequestParam TipoComida tipo,
            @RequestParam LocalDate dia) {

        Map<String,String> resp = new HashMap<>();
        try {
            comidaFavoritaService.agregarComidaFavoritaaIngerida(nombrePaquete, tipo, dia);
            resp.put("mensaje", "Paquete agregado al día correctamente.");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resp);

        } catch (Exception e) {
            resp.put("error", e.getMessage());
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resp);
        }
    }





}
