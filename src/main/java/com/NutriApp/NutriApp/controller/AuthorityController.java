package com.NutriApp.NutriApp.controller;

import com.NutriApp.NutriApp.exceptions.AuthorityInvalidaException;
import com.NutriApp.NutriApp.modelo.Authority;
import com.NutriApp.NutriApp.service.AuthorityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rol")
@Tag(name = "Roles", description = "Operaciones con los roles")
public class AuthorityController {

    private final AuthorityService authorityService;

    @Operation(summary = "Cambiar rol a ADMIN.", description = "Cambia el rol de un usuario a ADMIN.")
    @PostMapping("/cambiar/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cambiarRolAdmin (@RequestParam String username) throws AuthorityInvalidaException {
        authorityService.cambiaRol_A_ADMIN(username);

        return ResponseEntity.ok("Se cambio el rol a ADMIN correctamente");
    }

    @Operation(summary = "Cambiar rol a CLIENTE.", description = "Cambia el rol de un usuario a CLIENTE.")
    @PostMapping("/cambiar/cliente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cambiarRolCliente (@RequestParam String username) throws AuthorityInvalidaException {
        authorityService.cambiaRol_A_CLIENTE(username);

        return ResponseEntity.ok("Se cambio el rol a CLIENTE correctamente");
    }

    @Operation(summary = "Obtener el rol del usuarioLogeado")
    @GetMapping("/obtener")
    public ResponseEntity<Authority> obtenerRolLogeado () throws AuthorityInvalidaException{
        return  ResponseEntity.ok(authorityService.obtenerRolLogeado());
    }
}
