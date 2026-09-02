package com.NutriApp.NutriApp.modelo.dto;

import com.NutriApp.NutriApp.modelo.enums.EstadoDia;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class EstadoDiaDTO {

    private LocalDate fecha;
    private EstadoDia estadoDia;
    private double caloriasConsumidas;
    private double objetivoCalorico;


}
