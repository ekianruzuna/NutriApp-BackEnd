package com.NutriApp.NutriApp.modelo.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HidratacionSalidaDTO {
    private Long id;
    private Integer cantidadMl;
    private LocalDate fecha;
}
