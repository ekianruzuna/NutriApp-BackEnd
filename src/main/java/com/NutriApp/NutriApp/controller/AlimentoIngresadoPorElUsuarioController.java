package com.NutriApp.NutriApp.controller;

import com.NutriApp.NutriApp.modelo.AlimentoIngresadoPorUsuario;
import com.NutriApp.NutriApp.service.AlimentoIngresadoPorUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "Listar los utlimos 10 alimentos en nuestra BDD.", description = "Devuleve una lista con los ultimos 10 alimentos obtenidos de nuestra BDD.")
    @GetMapping("/listarUltimos10")
    public ResponseEntity<List<AlimentoIngresadoPorUsuario>> listarUltimos10 (){
        return ResponseEntity.ok(alimentoIngresadoPorUsuarioService.listarUtimos10());
    }

    //solo admins
    @Operation(summary = "Buscar alimentos en nuestra bdd.", description = "Devuleve una lista con los alimentos que matchean con el nombre de nuestra BDD.")
    @GetMapping("/filtrar")
    public ResponseEntity<List<AlimentoIngresadoPorUsuario>> filtrarPorNombreComida (@RequestParam String nombreComida){
        return ResponseEntity.ok(alimentoIngresadoPorUsuarioService.filtrarAlimentosPorNombreComida(nombreComida));
    }

    @Operation(summary = "Insertar un alimento en nuestra bdd.", description = "Inserta un alimento en nuestra BDD.")
    @PostMapping("/insert")
    public ResponseEntity<AlimentoIngresadoPorUsuario> insertar (@RequestBody @Validated AlimentoIngresadoPorUsuario alimentoIngresadoPorUsuario){
        return  ResponseEntity.ok(alimentoIngresadoPorUsuarioService.insertar(alimentoIngresadoPorUsuario));
    }

    @Operation(summary = "Editar un alimento en nuestra bdd.", description = "Edita un alimento en nuestra BDD.")
    @PutMapping("/editar")
    public ResponseEntity<AlimentoIngresadoPorUsuario> editar (@RequestParam long idAlimento, @RequestBody @Validated AlimentoIngresadoPorUsuario alimentoIngresadoPorUsuario){
        return  ResponseEntity.ok(alimentoIngresadoPorUsuarioService.editar(idAlimento, alimentoIngresadoPorUsuario));
    }

    @Operation(summary = "Eliminar un alimento en nuestra bdd.", description = "Elmina un alimento en nuestra BDD.")
    @DeleteMapping("/eliminar")
    public ResponseEntity<String> eliminar (@RequestParam long idAlimento){
        return  ResponseEntity.ok(alimentoIngresadoPorUsuarioService.eliminar(idAlimento));
    }



}
