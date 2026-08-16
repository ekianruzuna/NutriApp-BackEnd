package com.NutriApp.NutriApp.repository;

import com.NutriApp.NutriApp.modelo.Comida;
import com.NutriApp.NutriApp.modelo.ComidaIngerida;
import com.NutriApp.NutriApp.modelo.enums.TipoComida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.awt.print.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComidaIngeridaRepository extends JpaRepository<ComidaIngerida, Long> {

    @Query("""
            SELECT ci FROM ComidaIngerida ci
            JOIN ci.dia d
            JOIN d.usuario u
            WHERE u.persona.id = :userId
              AND d.fecha = :fecha
              AND ci.idComidaApi = :comidaId
              AND ci.tipoComida = :tipoComida
            """)
    Optional<ComidaIngerida> findByUserIdAndFechaAndComidaIdAndTipoComida(
            @Param("userId") int userId,
            @Param("fecha") LocalDate fecha,
            @Param("comidaId") Long comidaId,
            @Param("tipoComida") TipoComida tipoComida);

    // 1. Resumen por fechas
    @Query("SELECT c FROM ComidaIngerida c " +
            "JOIN c.dia d " +
            "JOIN d.usuario u " +
            "WHERE u.username = :username " +
            "AND d.fecha BETWEEN :inicio AND :fin")
    List<ComidaIngerida> findByUsernameAndFechaBetween(
            @Param("username") String username,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin
    );


    // Cambiamos el tipo de retorno a String y seleccionamos solo el nombre
    @Query("SELECT c.nombreComida FROM ComidaIngerida c " +
            "JOIN c.dia d " +
            "JOIN d.usuario u " +
            "WHERE u.username = :username " +
            "GROUP BY c.nombreComida " +
            "ORDER BY COUNT(c.nombreComida) DESC")
    List<String> findTopFrecuenciaNombres(@Param("username") String username);
}