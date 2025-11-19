package com.NutriApp.NutriApp.controller;

import com.NutriApp.NutriApp.modelo.dto.PerfilNutricionalDTO;
import com.NutriApp.NutriApp.modelo.dto.PersonaDTO;
import com.NutriApp.NutriApp.modelo.dto.UsuarioDTO;
import com.NutriApp.NutriApp.exceptions.PersonaInvalidaException;
import com.NutriApp.NutriApp.modelo.Persona;
import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.service.PerfilNutricionalService;
import com.NutriApp.NutriApp.service.PersonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Validated
@RestController
@RequestMapping("/api/persona")
@RequiredArgsConstructor
@Tag(name = "Persona", description = "Operaciones con las personas")
public class PersonaController {

    private final PerfilNutricionalService perfilNutricionalService;


    // No es necesario usar @Autowired para inyectar la instancia de PersonaService,
    // ya que Spring detecta que esta clase (PersonaController) tiene un único constructor
    // que recibe como parámetro un bean (PersonaService) (aca se lo especificamos ocn el @RequiredArgsConstructor).
    // Spring automáticamente realiza la inyección de dependencias usando ese constructor.
    // Si existieran múltiples constructores, Spring no sabría cuál usar y se necesitaría
    // especificar la inyección de otra manera (por ejemplo, con @Autowired).
    private final PersonaService personaService;

    @Operation(summary = "Listar todas las personas del sistema.", description = "Devuelve una lista de todas las personas cargadas del sistema.")
    @GetMapping("/listar")
    public ResponseEntity<List<Persona>> obtenerTodasPersonas() {
        List<Persona> personas = personaService.obtenerTodas();
        if (personas.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(personas);
        }
    }

    //La exception que se lanza su no se encunetra la persona, en este metodo se vuelva a lanzar delegando la responsabilidad
    // a la clase GlobalExceptionHandler (buena practica)
    @Operation(summary = "Buscar persona.", description = "Devuelve los datos de una persona por un ID.")
    @GetMapping("/obtener")
    public ResponseEntity<Persona> obtenerPersonaXid(@RequestParam int id) throws PersonaInvalidaException {
        return ResponseEntity.ok(personaService.obtenerPorId(id));
    }

    @Operation(summary = "Buscar persona.", description = "Devuelve los datos de una persona por un ID.")
    @PostMapping("/guardar")
    public ResponseEntity<String> guardarPersona(@RequestBody @Validated Persona persona) throws PersonaInvalidaException {
        personaService.guardar(persona);
        return ResponseEntity.status(HttpStatus.CREATED).body("Persona guardada correctamente");
    }


    @Operation(summary = "Obtener mis datos.", description = "Devuelve los datos de persona del usuario logeado.")
    @GetMapping("/mostrarMisDatos")
    public ResponseEntity<Persona> mostrarPerfil() throws PersonaInvalidaException {
        return ResponseEntity.ok(personaService.obtenerMiPerfil());
    }


    @Operation(summary = "Modificar datos de persona.", description = "Modifica y devuelve los datos de persona actualizada del usuario logeado.")
    @PutMapping("/actualizar-persona")
    public ResponseEntity<Persona> actualizarPersona(@Valid @RequestBody PersonaDTO personaDTO) {
        return ResponseEntity.ok(personaService.actualizarDatosPersona(personaDTO));
    }



}
