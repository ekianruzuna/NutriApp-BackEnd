package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.exceptions.DiaInvalidoException;
import com.NutriApp.NutriApp.mapper.DiaMapper;
import com.NutriApp.NutriApp.modelo.*;
import com.NutriApp.NutriApp.modelo.dto.DiaDTO;
import com.NutriApp.NutriApp.repository.DiaRepository;
import com.NutriApp.NutriApp.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaService {

    private final DiaRepository diaRepository;
    private final UsuarioService usuarioService;


    // Crear nuevo dia
    public void guardar(Dia dia) {

        diaRepository.save(dia);
    }


    // Devuelve un día por fecha, o lo crea si no existe
    public Dia obtenerODiaOCrear(LocalDate fecha) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        return diaRepository.findByFechaAndUsuario(fecha, user)
                .orElseGet(() -> {
                    Dia nuevoDia = new Dia();
                    nuevoDia.setFecha(fecha);
                    nuevoDia.setUsuario(user);

                    // Inicializar la hidratación
                    Hidratacion nuevaHidratacion = new Hidratacion();
                    nuevaHidratacion.setCantidadMl(0); // Valor inicial
                    nuevaHidratacion.setDia(nuevoDia); // Vínculo bidireccional obligatorio

                    nuevoDia.setHidratacion(nuevaHidratacion); // Asignar al día

                    guardar(nuevoDia);
                    return nuevoDia;
                });
    }

    // Obtener un día por fecha
    public Optional<Dia> obtenerDiaPorFecha(LocalDate fecha, Usuario usuario) {
        return diaRepository.findByFechaAndUsuario(fecha, usuario);
    }


    public DiaDTO verDiaCompleto(LocalDate fecha) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        Dia dia = obtenerODiaOCrear(fecha);

        // Convertimos la entidad a DTO
        return DiaMapper.toDiaDTO(dia);
    }


    public List<Dia> verHistorialDias() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();
        Usuario usuarioConDias = usuarioService.obtenerUsuarioConDias(user.getUsername());

        List<Dia> dias = usuarioConDias.getDias();

        if (dias.isEmpty()) {
            throw new DiaInvalidoException("El usuario todavia no tiene dias cargados ");
        }

        return dias;

    }

    public void caloriasRestantesDia(LocalDate fecha) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();
        double objetivoDiario = user.getPerfilNutricional().getObjetivoDiario();
        Optional<Dia> dia = diaRepository.findByFechaAndUsuario(fecha, user);
        if (dia.isEmpty()) {
            throw new DiaInvalidoException("No existe un día cargado para ese usuario");
        }

        Dia diaActual = dia.get();

        List<ActividadFisica> actividadesFisicas = diaActual.getActividadesFisicasRealizadas();
        if (actividadesFisicas != null && !actividadesFisicas.isEmpty()) {

            for (ActividadFisica actividad : actividadesFisicas) {
                objetivoDiario += actividad.getCaloriasGastadas();
            }
        }

        List<ComidaIngerida> comidasIngeridas = diaActual.getComidasIngeridas();
        double caloriasConsumidas = 0;
        if (comidasIngeridas != null && !comidasIngeridas.isEmpty()) {
            for (ComidaIngerida comida : comidasIngeridas) {
                caloriasConsumidas += comida.getCalorias();
            }
        }

        diaActual.setCaloriasRestantes(objetivoDiario - caloriasConsumidas);

        guardar(diaActual);
    }

}

