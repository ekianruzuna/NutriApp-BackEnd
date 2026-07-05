package com.NutriApp.NutriApp.repository;

import com.NutriApp.NutriApp.modelo.Logros.HistorialLogro;
import com.NutriApp.NutriApp.modelo.enums.TipoLogro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface HistorialLogroRepository extends JpaRepository<HistorialLogro, Long> {

    boolean existsByUsuarioUsernameAndLogroObtenido_TipoLogroAndFechaObtencion(String username, TipoLogro tipoLogro, LocalDate fecha);
}
