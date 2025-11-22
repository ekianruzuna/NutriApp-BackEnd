package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.modelo.PerfilNutricional;
import com.NutriApp.NutriApp.modelo.dto.PerfilNutricionalDTO;
import com.NutriApp.NutriApp.modelo.dto.PersonaDTO;
import com.NutriApp.NutriApp.modelo.dto.UsuarioDTO;
import com.NutriApp.NutriApp.exceptions.PersonaInvalidaException;
import com.NutriApp.NutriApp.modelo.PerfilNutricional;
import com.NutriApp.NutriApp.modelo.Persona;
import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.dto.PersonaDTO;
import com.NutriApp.NutriApp.modelo.enums.Genero;
import com.NutriApp.NutriApp.repository.PersonaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.model.source.spi.PluralAttributeElementSourceAssociation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
public class PersonaService {

    // No es necesario usar @Autowired para inyectar la instancia de PersonaRepository,
    // ya que Spring detecta que esta clase (PersonaService) tiene un único constructor
    // que recibe como parámetro un bean (PersonaService) (aca lo especificamos con @RequierdArgsContructor).
    // Spring automáticamente realiza la inyección de dependencias usando ese constructor.
    // Si existieran múltiples constructores, Spring no sabría cuál usar y se necesitaría
    // especificar la inyección de otra manera (por ejemplo, con @Autowired).
    private final PersonaRepository personaRepository;
    private final PerfilNutricionalService perfilNutricionalService;


    public List<Persona> obtenerTodas() {
        return personaRepository.findAll();
    }

    // Obtener persona por ID
    public Persona obtenerPorId(int id) throws PersonaInvalidaException {
        return personaRepository.findById(id)
                .orElseThrow(() -> new PersonaInvalidaException("Persona no encontrada con ID: " + id));
    }

    // Obtener datos de mi perfil
    public Persona obtenerMiPerfil() throws PersonaInvalidaException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = (Usuario) auth.getPrincipal(); // si tu clase Usuario implementa UserDetails
        Persona persona = usuario.getPersona();
        return persona;
    }

    public boolean existsByDni(String dni) {
        if (personaRepository.existsByDni(dni)) {
            return true;
        }
        return false;
    }

    // Crear nueva persona
    public void guardar(Persona persona) throws PersonaInvalidaException {
        if (personaRepository.existsByDni(persona.getDni())) {
            throw new PersonaInvalidaException("La persona a ingresar ya se encuentra registrada");
        }
        personaRepository.save(persona);
    }

// METODO QUE ACTUALIZA LOS DATOS DE LA PERSONA
@Transactional
public Persona actualizarDatosPersona(PersonaDTO personaDTO) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Usuario user = (Usuario) auth.getPrincipal();

    Persona personaActual = (user.getPersona());

    // Validaciones de unicidad con stream
    Stream.of(
                    new AbstractMap.SimpleEntry<>("DNI",
                            !personaDTO.getDni().equals(personaActual.getDni()) && personaRepository.existsByDni(personaDTO.getDni())),
                    new AbstractMap.SimpleEntry<>("EMAIL",
                            !personaDTO.getEmail().equals(personaActual.getEmail()) && personaRepository.findByEmail(personaDTO.getEmail()).isPresent()),
                    new AbstractMap.SimpleEntry<>("TELÉFONO",
                            !personaDTO.getTelefono().equals(personaActual.getTelefono()) && personaRepository.findByTelefono(personaDTO.getTelefono()).isPresent())
            ).filter(Map.Entry::getValue)
            .findFirst()
            .ifPresent(entry -> {
                throw new PersonaInvalidaException("El " + entry.getKey() + " ingresado ya pertenece a otro usuario");
            });

    // Actualización de datos
    personaActual.setNombre(personaDTO.getNombre());
    personaActual.setApellido(personaDTO.getApellido());
    personaActual.setDni(personaDTO.getDni());
    personaActual.setFechaNacimiento(personaDTO.getFechaNacimiento());
    personaActual.setTelefono(personaDTO.getTelefono());
    personaActual.setDireccion(personaDTO.getDireccion());
    personaActual.setEmail(personaDTO.getEmail());

    // Si el género cambió, recalcular perfil nutricional
    if (!personaDTO.getGenero().equals(personaActual.getGenero())) {
        PerfilNutricionalDTO perfilDTO = new PerfilNutricionalDTO(
                user.getPerfilNutricional().getPeso(),
                user.getPerfilNutricional().getAltura(),
                user.getPerfilNutricional().getNivelActividadFisica(),
                user.getPerfilNutricional().getEdad(),
                user.getPerfilNutricional().getObjetivoCaloricoTipo()
        );
        user.setPerfilNutricional(perfilNutricionalService.realizar_calculo_BMR(perfilDTO, personaDTO.getGenero()));
    }

    personaActual.setGenero(personaDTO.getGenero());
    personaRepository.save(personaActual);

    return personaActual;
}

    public Persona obtenerPorUsername(String username) throws PersonaInvalidaException {
        return personaRepository.findByUsuarioUsername(username).
                orElseThrow(() -> new PersonaInvalidaException("Persona no encontrada con el username = " + username));
    }

    public boolean existeOtraPersonaConDNI(String dni) {
        return personaRepository.existsByDni(dni);
    }

    public boolean existeOtraPersonaConEmail(String email) {
        return personaRepository.existsByEmail(email);
    }

    public boolean existeOtraPersonaConTelefono(String telefono) {
        return personaRepository.existsByTelefono(telefono);
    }
}
