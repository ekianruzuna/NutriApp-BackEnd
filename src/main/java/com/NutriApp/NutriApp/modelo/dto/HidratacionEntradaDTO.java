package com.NutriApp.NutriApp.modelo.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor // OBLIGATORIO para que Jackson cree la instancia
@AllArgsConstructor // Útil para construir el objeto
public class HidratacionEntradaDTO {
    private Integer cantidadMl;
}
