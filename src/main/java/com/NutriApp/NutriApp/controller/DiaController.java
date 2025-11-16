package com.NutriApp.NutriApp.controller;


import com.NutriApp.NutriApp.modelo.Dia;
import com.NutriApp.NutriApp.modelo.dto.DiaDTO;
import com.NutriApp.NutriApp.service.DiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("dias")
@RequiredArgsConstructor
@Tag(name = "Dia", description = "Calendario de las actividades realizadas y comidas ingeridas")
public class DiaController {


    private final DiaService diaService;

    @Operation(summary = "Ver día completo", description = "Devuelve un día con las comidas ingeridas y actividades realizadas.")
    @GetMapping("/ver/completo")
    public ResponseEntity<DiaDTO> verDiaCompleto(@RequestParam LocalDate fecha) {
        return ResponseEntity.ok(diaService.verDiaCompleto(fecha));
    }


    @Operation(summary = "Ver dias.", description = "Devuelve una lista de dias con las comidas ingeridas y actividades realizadas.")
    @GetMapping("ver/todos")
    public ResponseEntity<List<Dia>> verHistorialDias(){

        return ResponseEntity.ok(diaService.verHistorialDias());

    }


}