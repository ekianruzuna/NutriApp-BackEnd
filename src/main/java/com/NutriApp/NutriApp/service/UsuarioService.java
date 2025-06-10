package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.exceptions.AuthorityInvalidaException;
import com.NutriApp.NutriApp.exceptions.PersonaInvalidaException;
import com.NutriApp.NutriApp.exceptions.UsuarioInvalidoException;
import com.NutriApp.NutriApp.modelo.Authority;
import com.NutriApp.NutriApp.modelo.Persona;
import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.enums.Role;
import com.NutriApp.NutriApp.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaService personaService;
    private final AuthorityService authorityService;
    @PersistenceContext
    private EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;



    //Ese método es obligatorio si estás usando Spring Security con autenticación basada en formulario(formLogin()),
    //porque Spring necesita saber cómo obtener al usuario desde tu base de datos.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    public Usuario obtener (String username) throws UsuarioInvalidoException {
        return usuarioRepository.findById(username)
                .orElseThrow(() -> new UsuarioInvalidoException(username));
    }



    //este metodo inserta un usuario con rol cliente
    // por defecto a una persona ya registrada
    @Transactional
    public void insertarUsuarioCliente (Usuario usuario, int idPerosonaBuscar) throws PersonaInvalidaException, UsuarioInvalidoException, AuthorityInvalidaException{

        if (usuarioRepository.existsById(usuario.getUsername())){
            throw new UsuarioInvalidoException("El usuario ya existe con el username = " +usuario.getUsername());
        }

        Persona persona = personaService.obtenerPorId(idPerosonaBuscar);

        if (persona.getUsuario() != null){
            throw new PersonaInvalidaException("La persona ya tiene un usuario asociado");
        }

        Authority authority = Authority.builder()

                .role(Role.ROL_CLIENT)      //se setea por defecto en este metodo el rol de cliente
                .usuario(usuario)
                .build();


        usuario.setPersona(persona);

        usuario.setAuthority(authority);  //hace falta setearle los 3 objetos a cada uno porque tenemos una relacion bidireccional


        usuario.setPassword(passwordEncoder.encode(usuario.getPassword())); //se cifra la contraseña
        usuario.setEnabled(true);    //se setea la cuenta como activa



        //----IMPORTANTE----//        // Limpia el contexto de persistencia (entityManager) para evitar conflictos
//        entityManager.clear();        // con entidades duplicadas ya gestionadas en la sesión actual de Hibernate.
        //----IMPORTANTE----//        // En este caso, evita el error de identidad duplicada al asociar una Persona
                                      // ya cargada con un nuevo Usuario.



        usuarioRepository.save(usuario); //se guarda en la bdd
    }

    @Transactional
    public ResponseEntity<String> ActualizarUsuarioCliente (String username, Usuario usuarioActualizado) throws UsuarioInvalidoException{
        Usuario usuario = usuarioRepository.findById(username).orElseThrow(() -> new UsuarioInvalidoException(username));
        Persona persona = usuario.getPersona();
        Persona datosNuevos = usuarioActualizado.getPersona();

        persona.setNombre(datosNuevos.getNombre());
        persona.setApellido(datosNuevos.getApellido());
        persona.setDni(datosNuevos.getDni());
        persona.setFechaNacimiento(datosNuevos.getFechaNacimiento());
        persona.setEmail(datosNuevos.getEmail());
        persona.setTelefono(datosNuevos.getTelefono());
        persona.setDireccion(datosNuevos.getDireccion());
        persona.setGenero(datosNuevos.getGenero());

        personaService.guardar(persona);

        if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
        }

        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Perfil actualizado correctamente.");
    }
}

