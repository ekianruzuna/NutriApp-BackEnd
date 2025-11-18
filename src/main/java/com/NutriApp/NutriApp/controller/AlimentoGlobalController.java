package com.NutriApp.NutriApp.controller;

import com.NutriApp.NutriApp.modelo.dto.AlimentoBusquedaDTO;
import com.NutriApp.NutriApp.service.AlimentoGlobalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


//controladora para unificar los alimentos de nuestra bdd con la api
@RestController
@RequestMapping("/api/alimentos/global")
@Tag(name = "Alimentos Global", description = "Operaciones con alimentos de la api y nuestra bdd combinados")
public class AlimentoGlobalController {

    @Autowired
    private AlimentoGlobalService alimentoGlobalService;

    //cualquiera logeado
    @Operation(summary = "Buscar alimentos en la api y en nuestra bdd.", description = "Devuleve una lista con los alimentos obtenidos de la api y de nuestra bdd combinados.")
    @GetMapping("/buscar")
    public ResponseEntity<List<AlimentoBusquedaDTO>> buscarPorNombre (@RequestParam String nombreComida) throws Exception{
        return ResponseEntity.ok(alimentoGlobalService.filtrarAlimentosCombinadosPorNombreComida(nombreComida));
    }
}
