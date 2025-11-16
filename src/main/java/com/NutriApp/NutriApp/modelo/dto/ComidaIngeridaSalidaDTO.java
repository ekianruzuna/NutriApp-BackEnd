package com.NutriApp.NutriApp.modelo.dto;

import com.NutriApp.NutriApp.modelo.enums.TipoComida;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComidaIngeridaSalidaDTO {

    private Long id;
    private String nombreComida;
    private Double cantidad; // porciones o gramos, como lo manejes
    private TipoComida tipoComida;

    private Double calorias;
    private Double proteinas;
    private Double grasas;
    private Double carbohidratos;
}
