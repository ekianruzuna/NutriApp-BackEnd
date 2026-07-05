package com.NutriApp.NutriApp.modelo.Logros;


import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.enums.TipoLogro;
import com.NutriApp.NutriApp.service.DiaService;
import com.NutriApp.NutriApp.service.PerfilNutricionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class MetaDiariaEvaluator implements LogroEvaluator{

    private final DiaService diaService;
    private final PerfilNutricionalService perfilNutricionalService;

    @Override
    public boolean seCumple(LocalDate date, Usuario user) {
        //obtenemos las calorias consumidas en el dia
        double caloriasConsumidasDelDia = diaService.verCaloriasConsumidasDeunDia(date);

        //obtenenos el objetivo diario de consumo de calorias
        double metaDiariaCalorias = perfilNutricionalService.obtenerPerfilNutricional().getObjetivoDiario();

        //si cumplio el objetivo de calorias retornamos true
        return caloriasConsumidasDelDia >= metaDiariaCalorias;
    }

    @Override
    public TipoLogro getTipo() {
        return TipoLogro.META_CALORICA_DIARIA;
    }
}
