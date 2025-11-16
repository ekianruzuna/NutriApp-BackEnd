package com.NutriApp.NutriApp.modelo.dto;

import com.NutriApp.NutriApp.modelo.enums.NivelActividadFisica;
import com.NutriApp.NutriApp.modelo.enums.TipoActividadFisica;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActividadFisicaSalidaDTO {

    private TipoActividadFisica tipoActividad;

    private NivelActividadFisica intensidad;

    private double duracionMin;

    private double caloriasGastadas;
}