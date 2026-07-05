package com.NutriApp.NutriApp.repository;

import com.NutriApp.NutriApp.modelo.Logros.Logro;
import com.NutriApp.NutriApp.modelo.enums.TipoLogro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LogroRepository extends JpaRepository<Logro, Long> {
    boolean existsByTipoLogro (TipoLogro tipoLogro);
    Optional<Logro> findByTipoLogro (TipoLogro tipoLogro);
}
