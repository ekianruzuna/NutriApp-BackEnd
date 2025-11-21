package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.repository.PersonaRepository;
import com.NutriApp.NutriApp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioValidationService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;

    // USERNAME
    public boolean usernameDisponible(String username) {
        return !usuarioRepository.existsByUsername(username);
    }

    // EMAIL
    public boolean emailDisponible(String email) {
        return !personaRepository.existsByEmail(email);
    }

    // DNI
    public boolean dniDisponible(String dni) {
        return !personaRepository.existsByDni(dni);
    }

    // TELEFONO
    public boolean telefonoDisponible(String telefono) {
        return !personaRepository.existsByTelefono(telefono);
    }
}
