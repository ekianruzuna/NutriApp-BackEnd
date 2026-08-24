package com.NutriApp.NutriApp.modelo.Logros;

import com.NutriApp.NutriApp.modelo.Dia;
import com.NutriApp.NutriApp.modelo.Hidratacion;
import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.enums.TipoLogro;
import com.NutriApp.NutriApp.repository.HidratacionRepository;
import com.NutriApp.NutriApp.service.DiaService;
import com.NutriApp.NutriApp.service.HidratacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HidratacionDiariaEvaluator implements LogroEvaluator{

    private final DiaService diaService;
    private final HidratacionRepository hidratacionRepository;

    @Override
    public boolean seCumple(LocalDate date, Usuario user) {
        Dia dia = diaService.obtenerODiaOCrear(date);

        Optional<Hidratacion> totalAguaConsumida = hidratacionRepository.findByDia(dia);

        if (totalAguaConsumida.isEmpty()){
            return false;
        }

        return (totalAguaConsumida.get().getCantidadMl() >= 2000);
    }

    @Override
    public TipoLogro getTipo() {
        return TipoLogro.HIDRTACION_DIARIA;
    }
}
