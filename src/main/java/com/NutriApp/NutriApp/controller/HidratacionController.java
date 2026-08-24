package com.NutriApp.NutriApp.controller;

import com.NutriApp.NutriApp.modelo.Hidratacion;
import com.NutriApp.NutriApp.modelo.dto.HidratacionEntradaDTO;
import com.NutriApp.NutriApp.modelo.dto.HidratacionSalidaDTO;
import com.NutriApp.NutriApp.service.HidratacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/hidratacion")
@RequiredArgsConstructor
public class HidratacionController {

    private final HidratacionService hidratacionService;

    @PostMapping
    public ResponseEntity<HidratacionSalidaDTO> registrar(
            @RequestBody HidratacionEntradaDTO request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        // Si no se envía fecha, usamos la de hoy
        LocalDate fechaRegistro = (fecha != null) ? fecha : LocalDate.now();

        return ResponseEntity.ok(hidratacionService.registrarHidratacion(request, fechaRegistro));
    }

    @GetMapping("/total")
    public ResponseEntity<Integer> obtenerTotalPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        Integer total = hidratacionService.obtenerTotalAguaPorFecha(fecha);
        return ResponseEntity.ok(total);
    }
}
