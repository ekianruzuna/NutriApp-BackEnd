package com.NutriApp.NutriApp.repository;

import com.NutriApp.NutriApp.modelo.SolicitudAltaAlimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRespository extends JpaRepository<SolicitudAltaAlimento, Integer> {

    Optional<SolicitudAltaAlimento> findById (long id);
    Optional<SolicitudAltaAlimento> findByNombreComidaIgnoreCase(String nombreComidaSolictudBuscar);
    boolean existsByNombreComidaIgnoreCase(String nombreComida);
    List<SolicitudAltaAlimento> findAllByUsuarioUsername (String username);
    List<SolicitudAltaAlimento> findAllByNombreComidaIgnoreCase(String nombreComida);
    void deleteByUsuarioUsernameAndNombreComidaIgnoreCase(String username, String nombreComida);
    boolean existsByUsuarioUsernameAndNombreComidaIgnoreCase(String username, String nombreComida);
    Optional<SolicitudAltaAlimento> findByUsuarioUsernameAndNombreComidaIgnoreCase (String username, String nombreComida);
    void deleteById (long id);
    List<SolicitudAltaAlimento> findAllByFecha (LocalDateTime fecha);

}
