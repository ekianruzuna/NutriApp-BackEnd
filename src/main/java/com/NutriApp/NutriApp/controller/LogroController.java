package com.NutriApp.NutriApp.controller;

import com.NutriApp.NutriApp.modelo.Logros.HistorialLogro;
import com.NutriApp.NutriApp.modelo.enums.TipoLogro;
import com.NutriApp.NutriApp.service.LogroService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Logros", description = "Operaciones con los logros")
@RequestMapping("/api/logro")
public class LogroController {

    private final LogroService logroService;

    @GetMapping("/prueba")
    public ResponseEntity<String> logroPrueba(@RequestParam LocalDate fechaComprobar){
        logroService.comprobarYGuardarTodosLosLogros(fechaComprobar);
        return ResponseEntity.ok("Ya se comprobo");
    }

    @GetMapping("/obtener/veces/ganado")
    public ResponseEntity<Long> obtenerCantidadVecesLogro(@RequestParam TipoLogro tipoLogro){
        return ResponseEntity.ok(logroService.obtenerCantidadVecesLogroObtenido(tipoLogro));
    }

    @GetMapping("/listar/historial")
    public ResponseEntity<List<HistorialLogro>> listarHistorial (){
        return ResponseEntity.ok(logroService.obtenerHistorialLogros());
    }
}
