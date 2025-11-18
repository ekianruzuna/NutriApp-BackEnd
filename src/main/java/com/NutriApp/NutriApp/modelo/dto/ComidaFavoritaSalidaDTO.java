package com.NutriApp.NutriApp.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor  // ⚠️ Esto crea un constructor con todos los campos
@NoArgsConstructor
public class ComidaFavoritaSalidaDTO {
    private String nombrePaquete;
    private String nombreComida;
    private long comidaId;
    private double cantidad;
}
