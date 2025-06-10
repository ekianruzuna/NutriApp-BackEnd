package com.NutriApp.NutriApp.controller;

import com.NutriApp.NutriApp.exceptions.AuthorityInvalidaException;
import com.NutriApp.NutriApp.exceptions.PersonaInvalidaException;
import com.NutriApp.NutriApp.exceptions.UsuarioInvalidoException;
import com.NutriApp.NutriApp.modelo.Authority;
import com.NutriApp.NutriApp.modelo.Persona;
import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.enums.Role;
import com.NutriApp.NutriApp.repository.AuthorityRepository;
import com.NutriApp.NutriApp.repository.PersonaRepository;
import com.NutriApp.NutriApp.repository.UsuarioRepository;
import com.NutriApp.NutriApp.service.PersonaService;
import com.NutriApp.NutriApp.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;


    //Este metodo esta mal ya que antes se tenia instancias de los repositories en este controler
    //y eso es responsabilidad del service de cada clase
    /*@PostMapping("/registro")
    public ResponseEntity<String> registrarUsuario(@RequestBody Usuario request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("El usuario ya existe.");
        }

        Persona persona = new Persona();
        persona.setNombre(request.getPersona().getNombre());
        persona.setApellido(request.getPersona().getApellido());
        persona.setDni(request.getPersona().getDni());
        persona.setGenero(request.getPersona().getGenero());
        persona.setFechaNacimiento(request.getPersona().getFechaNacimiento());
        persona.setTelefono(request.getPersona().getTelefono());
        persona.setDireccion(request.getPersona().getDireccion());
        persona.setEmail(request.getPersona().getEmail());
        personaRepository.save(persona);

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEnabled(true);
        usuario.setPersona(persona);
        usuario.setAuthority(Authority.builder()
                .role(Role.ROL_CLIENT)
                .username(request.getUsername())
                .build());

        authorityRepository.save(usuario.getAuthority());


        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Usuario registrado correctamente.");
    }*/


    @PostMapping("/registro")
    public ResponseEntity<String> registrarUsuarioCliente (@RequestBody @Validated Usuario usuario, @RequestParam int idPerosonaBuscar) throws PersonaInvalidaException, UsuarioInvalidoException, AuthorityInvalidaException {
        usuarioService.insertarUsuarioCliente(usuario, idPerosonaBuscar);

        return ResponseEntity.ok("Usuario registrado correctamente como cliente");
    }
    @PutMapping("/perfil")
    public ResponseEntity<String> actualizarPerfil (@RequestBody Usuario usuarioActualizado, Principal principal) throws AuthorityInvalidaException, PersonaInvalidaException, UsuarioInvalidoException {
        String username = principal.getName();
        boolean actualizado = usuarioService.ActualizarUsuarioCliente(username, usuarioActualizado).hasBody();
        return ResponseEntity.ok(("Usuario actualizado correctamente como cliente"));
    }

}

