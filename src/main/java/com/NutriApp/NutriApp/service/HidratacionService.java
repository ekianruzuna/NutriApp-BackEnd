package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.modelo.Dia;
import com.NutriApp.NutriApp.modelo.Hidratacion;
import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.dto.HidratacionEntradaDTO;
import com.NutriApp.NutriApp.modelo.dto.HidratacionSalidaDTO;
import com.NutriApp.NutriApp.modelo.enums.TipoLogro;
import com.NutriApp.NutriApp.repository.DiaRepository;
import com.NutriApp.NutriApp.repository.HidratacionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class HidratacionService {
    private final HidratacionRepository hidratacionRepository;
    private final DiaService diaService;
    private final DiaRepository diaRepository;
    private final LogroService logroService;

    @Transactional
    public HidratacionSalidaDTO registrarHidratacion(HidratacionEntradaDTO request, LocalDate fecha) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        // 1. Buscamos el día
        Dia dia = diaService.obtenerODiaOCrear(fecha);

        // 2. Buscamos si ya existe un registro de hidratación para este día
        // (Asegúrate de que tu repositorio tenga findByDia)
        Optional<Hidratacion> hidratacionExistente = hidratacionRepository.findByDia(dia);

        Hidratacion hidratacion;

        if (hidratacionExistente.isPresent()) {
            hidratacion = hidratacionExistente.get();
            hidratacion.setCantidadMl(hidratacion.getCantidadMl() + request.getCantidadMl());


        } else {
            hidratacion = Hidratacion.builder()
                    .cantidadMl(request.getCantidadMl())
                    .dia(dia)
                    .build();
        }

        if (hidratacion.getCantidadMl() < 0)
        {
            hidratacion.setCantidadMl(0);
        }


        // 3. Guardamos (Spring JPA actualizará si tiene ID, o creará si es nuevo)
        Hidratacion guardada = hidratacionRepository.save(hidratacion);

        // 4. Comprobamos si se gano el logro de hidratacion diaria
        logroService.comprobarYGuerdarLogro(fecha, TipoLogro.HIDRATACION_DIARIA);

        return HidratacionSalidaDTO.builder()
                .id(guardada.getId())
                .cantidadMl(guardada.getCantidadMl())
                .fecha(dia.getFecha())
                .build();
    }

    public Integer obtenerTotalAguaPorFecha(LocalDate fecha) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        // Buscamos el día del usuario para esa fecha específica
        return diaRepository.findByFechaAndUsuario(fecha, user)
                .flatMap(hidratacionRepository::findByDia)
                .map(Hidratacion::getCantidadMl)
                .orElse(0); // Si no hay día o no hay hidratación, devolvemos 0
    }
}