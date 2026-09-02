package com.NutriApp.NutriApp.repository;

import com.NutriApp.NutriApp.modelo.Logros.HistorialLogro;
import com.NutriApp.NutriApp.modelo.enums.TipoLogro;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HistorialLogroRepository extends JpaRepository<HistorialLogro, Long> {

    boolean existsByUsuarioUsernameAndLogroObtenido_TipoLogroAndFechaObtencion(String username, TipoLogro tipoLogro, LocalDate fecha);
    Optional<HistorialLogro> findByUsuarioUsernameAndLogroObtenido_TipoLogroAndFechaObtencion(String username, TipoLogro tipoLogro, LocalDate fecha);
    int countAllByUsuario_UsernameAndLogroObtenido_TipoLogro (String username, TipoLogro tipoLogro);
    List<HistorialLogro> findAllByUsuarioUsername(String username);
    Optional<HistorialLogro> findFirstByUsuarioUsernameOrderByFechaRegistroDesc (String username);
    List<HistorialLogro> findAllByUsuarioUsernameAndLogroObtenido_TipoLogro(String username, TipoLogro tipoLogro);

}
