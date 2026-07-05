package com.NutriApp.NutriApp.Init;

import com.NutriApp.NutriApp.modelo.Logros.Logro;
import com.NutriApp.NutriApp.modelo.enums.TipoLogro;
import com.NutriApp.NutriApp.repository.LogroRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogroInitializer {

    private final LogroRepository logroRepository;

    //esta notacion le dice a spring que ejecute esto al inicializar la aplicacion
    @PostConstruct
    public void crear() {

        crearSiNoExiste("Meta Calorica Diaria", TipoLogro.META_CALORICA_DIARIA);
        crearSiNoExiste("Registro de inicio de sesion seguido", TipoLogro.LOGIN);
        crearSiNoExiste("Meta diaria de hidratacion", TipoLogro.HIDRTACION_DIARIA);

    }

    private void crearSiNoExiste(String descripcion, TipoLogro tipoLogro) {

        if (logroRepository.existsByTipoLogro(tipoLogro)) return;

        Logro logro = new Logro();
        logro.setTipoLogro(tipoLogro);
        logro.setDescripcion(descripcion);

        logroRepository.save(logro);
    }
}
