package com.NutriApp.NutriApp.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaDTO {

    private LocalDate fecha;
    private double caloriasRestantes;

    private List<ComidaIngeridaSalidaDTO> comidasIngeridas;
    private List<ActividadFisicaSalidaDTO> actividadesFisicasRealizadas;
}
