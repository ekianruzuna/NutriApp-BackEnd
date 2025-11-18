package com.NutriApp.NutriApp.repository;

import com.NutriApp.NutriApp.modelo.ComidaFavorita;
import com.NutriApp.NutriApp.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComidaFavoritaRepository extends JpaRepository<ComidaFavorita, Long> {

    // Ver si ya existe en ese paquete, para ese usuario
    boolean existsByNombrePaqueteAndComidaIdAndNombreComidaAndUsuario(
            String nombrePaquete, long comidaId, String nombreComida, Usuario usuario
    );


    // Listar todas las comidas favoritas de un paquete, para ese usuario
    List<ComidaFavorita> findAllByNombrePaqueteAndUsuario(
            String nombrePaquete, Usuario usuario
    );

    Optional<ComidaFavorita> findByNombrePaqueteAndComidaIdAndUsuario(
            String nombrePaquete, long comidaId, Usuario usuario
    );

    // Listar todas las comidas favoritas de un paquete, para ese usuario
    List<ComidaFavorita> findAllByUsuario(
            Usuario usuario
    );
}
