package com.NutriApp.NutriApp.repository;

import com.NutriApp.NutriApp.modelo.Dia;
import com.NutriApp.NutriApp.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaRepository extends JpaRepository<Dia, Long> {

    Optional<Dia> findByFechaAndUsuario(LocalDate fecha, Usuario usuario);

    // se busca todos los dias que esten asociados con el usuario y esten entre el
    // primer día hasta el último día del mes que se paso en el frontend
    List<Dia> findByUsuarioAndFechaBetween(Usuario usuario, LocalDate fechaInicio, LocalDate fechaFin);

    List<Dia> findByUsuario(Usuario usuario);

}