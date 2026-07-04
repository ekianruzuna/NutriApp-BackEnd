package com.NutriApp.NutriApp.repository;

import com.NutriApp.NutriApp.modelo.ComidaFavorita;
import com.NutriApp.NutriApp.modelo.Dia;
import com.NutriApp.NutriApp.modelo.Hidratacion;
import com.NutriApp.NutriApp.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface HidratacionRepository extends JpaRepository<Hidratacion, Long> {


    Optional<Hidratacion> findByDia(Dia dia);

}
