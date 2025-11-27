package com.NutriApp.NutriApp.controller;

import com.NutriApp.NutriApp.exceptions.SolicitudInvalidaException;
import com.NutriApp.NutriApp.modelo.SolicitudAltaAlimento;
import com.NutriApp.NutriApp.modelo.ValidacionBasica;
import com.NutriApp.NutriApp.modelo.ValidacionCompleta;
import com.NutriApp.NutriApp.service.SolicitudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.ValidationMode;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
@Validated
@RestController
@RequestMapping("/api/solicitud")
@RequiredArgsConstructor
@Tag(name = "Solicitudes", description = "Operaciones con las solicitudes de alta de comida")
public class SolicitudController {

    private final SolicitudService solicitudService;

    //cualquiera registrado
    @Operation(summary = "Realizar solicitud de alta de comida.", description = "Inserta una solicitud de alta de comida en la BBD.")
    @PostMapping("/insertar")
    public ResponseEntity<SolicitudAltaAlimento> insertar (@RequestBody @Validated(ValidacionCompleta.class) SolicitudAltaAlimento solicitudAltaAlimento) throws Exception {

        if (solicitudAltaAlimento.getPorcion() == 0.0){
            throw new IllegalArgumentException("El campo 'porcion' es obligatorio y no puede ser 0.0");
        }

        return ResponseEntity.ok(solicitudService.insertar(solicitudAltaAlimento));
    }

    //solo admins
    @Operation(summary = "Listar todas las solicitud.", description = "Devuelve una lista de todas las solicitudes de alta de comida del sistema.")
    @GetMapping("/listar/todas")
    public ResponseEntity<List<SolicitudAltaAlimento>> listarTodas (){
        return ResponseEntity.ok(solicitudService.listarTodas());
    }

    //solo admins
    @Operation(summary = "Filtrar todas las solicitudes de alta de comida por fecha.", description = "Devuelve una lista de solicitudes de alta de comida de todo el sistema por fecha.")
    @GetMapping("/filtrar/fecha")
    public ResponseEntity<List<SolicitudAltaAlimento>> filtrarDedeUnaFecha (@RequestParam LocalDate fechaFiltrar){
        return ResponseEntity.ok(solicitudService.filtrarSolicitudesPorFecha(fechaFiltrar));
    }

    //solo admins
    @Operation(summary = "Filtrar todas las solicitudes de alta de comida por username.", description = "Devuelve una lista de solicitudes de alta de comida de todo el sistema por username.")
    @GetMapping ("/filtrar/username")
    public ResponseEntity<List<SolicitudAltaAlimento>> filtrarPorUsername (@RequestParam String username){
        return ResponseEntity.ok(solicitudService.filtrarSolicitudesPorUsername(username));
    }

    //solo admins
    @Operation(summary = "Filtrar todas las solicitudes de alta de comida por nombre de comida.", description = "Devuelve una lista de solicitudes de alta de comida de todo el sistema por el nombre de comida.")
    @GetMapping("/filtrar/nombreComida")
    public ResponseEntity<List<SolicitudAltaAlimento>> filtrarPorNombreComida (@RequestParam String nombreComida){
        return ResponseEntity.ok(solicitudService.filtrarPorNombrecomida(nombreComida));
    }

    //cualquiera que este registrado
    @Operation(summary = "Listar todas mis solicitudes de alta de comida.", description = "Devuelve una lista de todas mis solicitudes de alta de comida.")
    @GetMapping("/listar/misSolicitudes")
    public ResponseEntity<List<SolicitudAltaAlimento>> listarMisSolicitudes (){
        return ResponseEntity.ok(solicitudService.listarMisSolicitudes());
    }

    //cualquiera que este registrado
    @Operation(summary = "Eliminar misolicitud de alta de comida.", description = "Elimina una solicitud de alta de comida de la BDD.")
    @DeleteMapping ("/eliminar")
    public ResponseEntity<String> eliminarMiSolicitud (@RequestParam String nombreComidaSolicitudEliminar){
        return ResponseEntity.ok(solicitudService.elimiarMiSolicitud(nombreComidaSolicitudEliminar));
    }

    //cualquiera que este registrado
    @Operation(summary = "Modificar una de mis solicitudes de alta de comida.", description = "Modifica una de las solicitudes de alta de comida.")
    @PutMapping("/modificar/miSolicitud")
    public ResponseEntity<SolicitudAltaAlimento> modificarMiSolicitud (@RequestParam String nombreComidaModificar, @RequestBody @Validated(ValidacionBasica.class) SolicitudAltaAlimento solicitudNueva){
        return ResponseEntity.ok(solicitudService.modificarMiSolicitud(nombreComidaModificar, solicitudNueva));
    }

    //solo admins
    @Operation(summary = "Modificar una solicitud y aceptarla", description = "Modifica la solicitud e ingresa el alimento en la bdd")
    @PutMapping("/modificarAndAceptar")
    public ResponseEntity<String> modificar_Y_Aceptar (@RequestParam String nombreComidaSolicitudModificar, @RequestBody @Validated(ValidacionBasica.class) SolicitudAltaAlimento solicitudAltaAlimentoNueva){
        return ResponseEntity.ok(solicitudService.modificar_Y_AceptarSolicitud(nombreComidaSolicitudModificar, solicitudAltaAlimentoNueva));
    }

    @Operation(summary = "Aceptar una solicitud de alta de comida.", description = "Acepta solicitud de alta de comida.")
    @PostMapping ("/aceptar")
    public ResponseEntity<String> aceptarSolicitud (@RequestParam long idSolicitud){
        return ResponseEntity.ok(solicitudService.aceptarSolicitud(idSolicitud));
    }

    //solo admins
    @Operation(summary = "Rechazar una solicitud de alta de comida.", description = "Rechaza solicitud de alta de comida.")
    @DeleteMapping ("/rechazar")
    public ResponseEntity<String> rechazarSolicitud (@RequestParam long idSolicitud){
        return ResponseEntity.ok(solicitudService.rechazarSolicitud(idSolicitud));
    }

}
