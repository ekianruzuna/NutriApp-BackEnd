package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.modelo.dto.ActividadFisicaResponseDTO;
import com.NutriApp.NutriApp.exceptions.ActividadFisicaInvalidaException;
import com.NutriApp.NutriApp.exceptions.DiaInvalidoException;
import com.NutriApp.NutriApp.modelo.ActividadFisica;
import com.NutriApp.NutriApp.modelo.Dia;
import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.enums.NivelActividadFisica;
import com.NutriApp.NutriApp.modelo.enums.TipoActividadFisica;
import com.NutriApp.NutriApp.repository.ActividadFisicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ActividadFisicaService {

    private final ActividadFisicaRepository actividadFisicaRepository;
    private final DiaService diaService;


    public void guardar(ActividadFisica actividadFisica) {

        actividadFisicaRepository.save(actividadFisica);

    }

    public void eliminar(ActividadFisica actividadFisica){
        actividadFisicaRepository.delete(actividadFisica);
    }



    public void agregarActividadFsicaRealizada(TipoActividadFisica tipoActividad, NivelActividadFisica intensidad, double duracionMin) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        Dia dia = diaService.obtenerODiaOCrear(user.getFechaActiva());

        ActividadFisica actividadFisica = new ActividadFisica();
        actividadFisica.setTipoActividad(tipoActividad);
        dia.getActividadesFisicasRealizadas().add(actividadFisica);
        actividadFisica.setDia(dia);
        actividadFisica.setIntensidad(intensidad);
        actividadFisica.setDuracionMin(duracionMin);
        actividadFisica.setCaloriasGastadas(actividadFisica.calcularCaloriasGastadas(user.getPerfilNutricional()));

        guardar(actividadFisica);

        diaService.caloriasRestantesDia(dia.getFecha());

    }


    public void agregarActividadFsicaRealizadaEnUnDiaEspecifico(TipoActividadFisica tipoActividad, NivelActividadFisica intensidad, double duracionMin, LocalDate fecha) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        Dia dia = diaService.obtenerODiaOCrear(fecha);

        ActividadFisica actividadFisica = new ActividadFisica();
        actividadFisica.setTipoActividad(tipoActividad);
        dia.getActividadesFisicasRealizadas().add(actividadFisica);
        actividadFisica.setDia(dia);
        actividadFisica.setIntensidad(intensidad);
        actividadFisica.setDuracionMin(duracionMin);
        actividadFisica.setCaloriasGastadas(actividadFisica.calcularCaloriasGastadas(user.getPerfilNutricional()));


        guardar(actividadFisica);

        diaService.caloriasRestantesDia(dia.getFecha());
    }

    public List<ActividadFisicaResponseDTO> obtenerTodas() {
        List<ActividadFisica> actividadFisicas = actividadFisicaRepository.findAll();
        if (actividadFisicas.isEmpty()) {
            throw new ActividadFisicaInvalidaException("No se encontraron actividades fisicas cargadas");
        }

        // DTO para poder asignarle el usuario y el dia en el que se agrego la actividad
        return actividadFisicas.stream().map(actividad ->
                new ActividadFisicaResponseDTO(
                        actividad.getId(),
                        actividad.getTipoActividad(), // tipoActividad
                        actividad.getIntensidad(),
                        actividad.getDuracionMin(),
                        actividad.getCaloriasGastadas(),
                        actividad.getDia().getFecha(),                      // fecha
                        actividad.getDia().getUsuario().getUsername()       // username
                )
        ).toList();
    }


    public List<ActividadFisica> obtenerTodasPorDia(LocalDate fecha) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        Dia dia = diaService.obtenerDiaPorFecha(fecha, user)
                .orElseThrow(() -> new DiaInvalidoException("No se encontro el dia registrado con fecha: " + fecha));

        List<ActividadFisica> actividadFisicasRealizadas = actividadFisicaRepository.findActividadFisicasByDia(dia);

        if (actividadFisicasRealizadas.isEmpty()) {
            throw new ActividadFisicaInvalidaException("No se encontraron actividades fisicas cargadas para el dia : " + dia.getFecha());
        }

        return actividadFisicasRealizadas;
    }

    public Optional<ActividadFisica> obtenerActividadPorFechaEid(Dia dia, long id) {
        return actividadFisicaRepository.findActividadFisicaByDiaAndId(dia, id);
    }


    public void elminarActividadFisica(LocalDate fecha, long idActividad) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        Dia dia = diaService.obtenerDiaPorFecha(fecha, user)
                .orElseThrow(() -> new DiaInvalidoException("No se encontro el dia registrado con fecha: " + fecha));

        ActividadFisica actividadFisica = obtenerActividadPorFechaEid(dia, idActividad)
                .orElseThrow(() -> new ActividadFisicaInvalidaException("No se encontro la actividad con id : " + idActividad));

        dia.getActividadesFisicasRealizadas().remove(actividadFisica);
        eliminar(actividadFisica);

        diaService.caloriasRestantesDia(dia.getFecha());


    }

    public List<ActividadFisica> obtenerActividadFisicaPorTipo(TipoActividadFisica tipo) {
        return actividadFisicaRepository.findActividadFisicaByTipoActividad(tipo);
    }

    public List<ActividadFisica> filtrarActividadFisicasDelSistema(TipoActividadFisica tipoActividad) {


        List<ActividadFisica> actividades = obtenerActividadFisicaPorTipo(tipoActividad);

        if (actividades.isEmpty()) {
            throw new ActividadFisicaInvalidaException("No se encontraron actividades fisicas cargadas con el tipo : " + tipoActividad);
        }

        return actividades;

    }

    public List<ActividadFisica> filtrarActividadesFisicasRealizadas(LocalDate fecha, TipoActividadFisica tipoActividad) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        Dia dia = diaService.obtenerDiaPorFecha(fecha, user)
                .orElseThrow(() -> new DiaInvalidoException("No se encontro el dia registrado con fecha: " + fecha));

        List<ActividadFisica> actividades = actividadFisicaRepository.findActividadFisicaByDiaAndTipoActividad(dia, tipoActividad);

        if (actividades.isEmpty()) {
            throw new ActividadFisicaInvalidaException("No se encontraron actividades fisicas cargadas con el tipo : " + tipoActividad + "con la fecha : " + fecha);
        }

        return actividades;

    }


    public void modificarActividadFisica(LocalDate fecha, long idActividad, TipoActividadFisica tipoActividad_modificar, NivelActividadFisica intensidad_modificar, double duracionMin_modificar) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        Dia dia = diaService.obtenerDiaPorFecha(fecha, user)
                .orElseThrow(() -> new DiaInvalidoException("No se encontro el dia registrado con fecha: " + fecha));

        ActividadFisica actividadFisica = obtenerActividadPorFechaEid(dia, idActividad)
                .orElseThrow(() -> new ActividadFisicaInvalidaException("No se encontro la actividad con id : " + idActividad));


        // Solo permitimos modificar si el tipo es igual (opcional, para mayor seguridad)
        if (!actividadFisica.getTipoActividad().equals(tipoActividad_modificar)) {
            throw new ActividadFisicaInvalidaException("No se puede cambiar el tipo de actividad. Solo se permiten modificaciones sobre el mismo tipo.");
        }


        actividadFisica.setTipoActividad(tipoActividad_modificar);
        dia.getActividadesFisicasRealizadas().add(actividadFisica);
        actividadFisica.setDia(dia);
        actividadFisica.setIntensidad(intensidad_modificar);
        actividadFisica.setDuracionMin(duracionMin_modificar);
        actividadFisica.setCaloriasGastadas(actividadFisica.calcularCaloriasGastadas(user.getPerfilNutricional()));

        guardar(actividadFisica); //  Guardo la nueva

        diaService.caloriasRestantesDia(dia.getFecha());

    }



    public List<String> obtenerTiposDisponibles() {
        return Arrays.stream(TipoActividadFisica.values())
                .map(Enum::name)
                .map(String::toLowerCase)
                .toList();
    }


}