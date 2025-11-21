package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.exceptions.UsuarioInexistenteException;
import com.NutriApp.NutriApp.modelo.*;
import com.NutriApp.NutriApp.modelo.dto.LoginRequest;
import com.NutriApp.NutriApp.modelo.dto.LoginResponse;
import com.NutriApp.NutriApp.modelo.dto.RegistroUsuarioRequest;
import com.NutriApp.NutriApp.exceptions.PersonaInvalidaException;
import com.NutriApp.NutriApp.exceptions.UsuarioExistente;
import com.NutriApp.NutriApp.modelo.enums.Role;
import com.NutriApp.NutriApp.repository.AuthorityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Autowired
    private PerfilNutricionalService perfilNutricionalService;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UsuarioService usuarioDetailsService;
    @Autowired
    private PersonaService personaService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MailService mailService;

    public LoginResponse login(@RequestBody LoginRequest request) {

        // Autenticamos al usuario con nombre y contraseña
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Obtenemos los detalles del usuario desde la base de datos
        UserDetails user = usuarioDetailsService.loadUserByUsername(request.getUsername());

        // Generamos el token JWT
        String token = jwtService.generateToken(user);

        // Devolvemos el token en la respuesta
        return new LoginResponse(token);
    }

    @Transactional
    public LoginResponse registrarUsuario(RegistroUsuarioRequest request) throws UsuarioExistente, PersonaInvalidaException {

        if (usuarioDetailsService.existsByUsername(request.getUsuario().getUsername())) {
            throw new UsuarioExistente("El nombre de usuario ya está en uso");
        }

        // Validaciones de unicidad con stream
        Stream.of(
                        new AbstractMap.SimpleEntry<>("DNI", personaService.existeOtraPersonaConDNI(request.getPersona().getDni())),
                        new AbstractMap.SimpleEntry<>("EMAIL", personaService.existeOtraPersonaConEmail(request.getPersona().getEmail())),
                        new AbstractMap.SimpleEntry<>("TELÉFONO", personaService.existeOtraPersonaConTelefono(request.getPersona().getTelefono()))
                ).filter(Map.Entry::getValue)
                .findFirst()
                .ifPresent(entry -> {
                    throw new PersonaInvalidaException("El " + entry.getKey() + " ingresado ya pertenece a otro usuario");
                });

        Persona persona = new Persona();
        persona.setNombre(request.getPersona().getNombre());
        persona.setApellido(request.getPersona().getApellido());
        persona.setDni(request.getPersona().getDni());
        persona.setGenero(request.getPersona().getGenero());
        persona.setFechaNacimiento(request.getPersona().getFechaNacimiento());
        persona.setTelefono(request.getPersona().getTelefono());
        persona.setDireccion(request.getPersona().getDireccion());
        persona.setEmail(request.getPersona().getEmail());



        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsuario().getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getUsuario().getPassword()));
        usuario.setEnabled(true);
        usuario.setPersona(persona);

        Authority authority = Authority.builder()
                .authority(Role.ROLE_CLIENT)
                .usuario(usuario)
                .build();

        usuario.setRole(authority);

        PerfilNutricional perfilNutricional = perfilNutricionalService.realizar_calculo_BMR(request.getPerfilNutricional(), persona.getGenero());
        usuario.setPerfilNutricional(perfilNutricional);

        usuarioDetailsService.guardar(usuario);

        //obtenemos los datos de la persona del usuario
        personaService.obtenerPorUsername(usuario.getUsername());

        //mandamos el mail con un aviso de creacion de cuenta
        mailService.enviarMail(persona.getEmail(), "Creacion de cuenta", "Su cuenta = '" +usuario.getUsername()+ "' fue creada con exito");


        // Cargar UserDetails para generar token
        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(request.getUsuario().getUsername());
        String token = jwtService.generateToken(userDetails);

        // Devolver token en la respuesta
        return new LoginResponse(token);
    }



}
