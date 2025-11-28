package com.NutriApp.NutriApp.repository;

import com.NutriApp.NutriApp.modelo.AlimentoIngresadoPorUsuario;
import com.NutriApp.NutriApp.modelo.dto.AlimentoBusquedaDTO;
import com.NutriApp.NutriApp.modelo.dto.MacronutrienteDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlimentoIngresadoPorUsuarioRepository extends JpaRepository<AlimentoIngresadoPorUsuario, Long> {

    boolean existsByNombreComida (String nombreComida);

    @Query("""
       SELECT new com.NutriApp.NutriApp.modelo.dto.MacronutrienteDTO(
           a.nombreComida,
           a.id,
           a.calorias,
           a.proteinas,
           a.grasas,
           a.carbohidratos,
           a.gramosPorPorcion
       )
       FROM alimentoIngresadoPorUsuario a
       WHERE a.nombreComida = :nombreComida AND a.id = :id
       """)
    Optional<MacronutrienteDTO> findMacronutrientesByNombreComidaAndId(@Param("nombreComida") String nombreComida, @Param("id") Long id);
    boolean existsByNombreComidaIgnoreCase(String nombreComida);
    boolean existsById (long id);
    Optional<AlimentoIngresadoPorUsuario> findById (long id);
    Optional<List<AlimentoIngresadoPorUsuario>> findAllByNombreComidaContainingIgnoreCase (String nombreComida);
    Optional<AlimentoIngresadoPorUsuario> findByNombreComidaContainingIgnoreCase (String nombreComida);
    Optional<AlimentoIngresadoPorUsuario> findByNombreComidaIgnoreCase (String nombreComida);
//    void deleteBy
    Optional<List<AlimentoIngresadoPorUsuario>> findTop10ByOrderByIdDesc ();
}
