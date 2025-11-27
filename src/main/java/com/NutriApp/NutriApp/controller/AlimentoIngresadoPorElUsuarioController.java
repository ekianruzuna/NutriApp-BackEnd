package com.NutriApp.NutriApp.controller;

import com.NutriApp.NutriApp.modelo.AlimentoIngresadoPorUsuario;
import com.NutriApp.NutriApp.service.AlimentoIngresadoPorUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alimentos-usuario")
@Tag(name = "Alimentos BDD", description = "Operaciones con alimentos nuestra BDD")
public class AlimentoIngresadoPorElUsuarioController {

    @Autowired
    private AlimentoIngresadoPorUsuarioService alimentoIngresadoPorUsuarioService;

    //solo admins
    @Operation(summary = "Listar todos los alimentos en nuestra BDD.", description = "Devuleve una lista con todos los alimentos obtenidos de nuestra BDD.")
    @GetMapping("/listarTodos")
    public ResponseEntity<List<AlimentoIngresadoPorUsuario>> listarTodos (){
        return ResponseEntity.ok(alimentoIngresadoPorUsuarioService.listarTodos());
    }

    //solo admins
    @Operation(summary = "Buscar alimentos en nuestra bdd.", description = "Devuleve una lista con los alimentos que matchean con el nombre de nuestra BDD.")
    @GetMapping("/filtrar")
    public ResponseEntity<List<AlimentoIngresadoPorUsuario>> filtrarPorNombreComida (@RequestParam String nombreComida){
        return ResponseEntity.ok(alimentoIngresadoPorUsuarioService.filtrarAlimentosPorNombreComida(nombreComida));
    }



}
