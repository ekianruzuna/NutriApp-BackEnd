package com.NutriApp.NutriApp.repository;

import com.NutriApp.NutriApp.modelo.Persona;
import com.NutriApp.NutriApp.modelo.Usuario;
import org.hibernate.boot.archive.internal.JarProtocolArchiveDescriptor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    boolean existsByUsername(String username); //aca no hace falta especificar la query que se va a lanzar con este metodo ya que hay

    @Query("SELECT u FROM Usuario u JOIN FETCH u.dias WHERE u.username = :username")
    Optional<Usuario> findByUsernameWithDias(@Param("username") String username);

    Optional<Usuario> findByPersonaEmail(String email);

    //algunos nombres de metodos como este caso que JPA los interpreta automaticamente.
    //Pero sino, se deberia de poner la firma del metodo y arriba la sentencia sql con @Query.
    //Y si la query es muy compleja se deberia de crear un DAO y delegarle el metodo

}
