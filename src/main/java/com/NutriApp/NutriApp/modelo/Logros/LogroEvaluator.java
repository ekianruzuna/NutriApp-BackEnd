package com.NutriApp.NutriApp.modelo.Logros;


import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.enums.TipoLogro;

import java.time.LocalDate;

public interface LogroEvaluator {
    boolean seCumple(LocalDate date, Usuario user);

    TipoLogro getTipo();
}
