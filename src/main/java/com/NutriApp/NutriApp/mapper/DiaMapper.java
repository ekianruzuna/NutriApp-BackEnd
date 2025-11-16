package com.NutriApp.NutriApp.mapper;

import com.NutriApp.NutriApp.modelo.ActividadFisica;
import com.NutriApp.NutriApp.modelo.ComidaIngerida;
import com.NutriApp.NutriApp.modelo.Dia;
import com.NutriApp.NutriApp.modelo.dto.ActividadFisicaSalidaDTO;
import com.NutriApp.NutriApp.modelo.dto.ComidaIngeridaSalidaDTO;
import com.NutriApp.NutriApp.modelo.dto.DiaDTO;

import java.util.stream.Collectors;

public class DiaMapper {

    public static DiaDTO toDiaDTO(Dia dia) {
        DiaDTO dto = new DiaDTO();
        dto.setFecha(dia.getFecha());
        dto.setCaloriasRestantes(dia.getCaloriasRestantes());

        // Mapear comidas ingeridas
        dto.setComidasIngeridas(
                dia.getComidasIngeridas().stream()
                        .map(DiaMapper::toComidaIngeridaSalidaDTO)
                        .collect(Collectors.toList())
        );

        // Mapear actividades físicas
        dto.setActividadesFisicasRealizadas(
                dia.getActividadesFisicasRealizadas().stream()
                        .map(DiaMapper::toActividadFisicaSalidaDTO)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    private static ComidaIngeridaSalidaDTO toComidaIngeridaSalidaDTO(ComidaIngerida comida) {
        ComidaIngeridaSalidaDTO dto = new ComidaIngeridaSalidaDTO();
        dto.setId(comida.getIdComidaApi());
        dto.setNombreComida(comida.getNombreComida());
        dto.setCalorias(comida.getCalorias());
        dto.setProteinas(comida.getProteinas());
        dto.setGrasas(comida.getGrasas());
        dto.setCarbohidratos(comida.getCarbohidratos());
        dto.setCantidad(comida.getCantidad());
        dto.setTipoComida(comida.getTipoComida());
        return dto;
    }

    private static ActividadFisicaSalidaDTO toActividadFisicaSalidaDTO(ActividadFisica actividad) {
        ActividadFisicaSalidaDTO dto = new ActividadFisicaSalidaDTO();
        dto.setTipoActividad(actividad.getTipoActividad());
        dto.setIntensidad(actividad.getIntensidad());
        dto.setDuracionMin(actividad.getDuracionMin());
        dto.setCaloriasGastadas(actividad.getCaloriasGastadas());
        return dto;
    }
}
