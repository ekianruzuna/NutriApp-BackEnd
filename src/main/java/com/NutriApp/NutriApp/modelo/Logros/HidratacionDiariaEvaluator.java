package com.NutriApp.NutriApp.modelo.Logros;

import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.enums.TipoLogro;
import com.NutriApp.NutriApp.service.DiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class HidratacionDiariaEvaluator implements LogroEvaluator{

    private final DiaService diaService;

    @Override
    public boolean seCumple(LocalDate date, Usuario user) {
        return true;
    }

    @Override
    public TipoLogro getTipo() {
        return TipoLogro.HIDRTACION_DIARIA;
    }
}
