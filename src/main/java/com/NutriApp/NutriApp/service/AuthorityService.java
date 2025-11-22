package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.exceptions.AuthorityInvalidaException;
import com.NutriApp.NutriApp.modelo.Authority;
import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.enums.Role;
import com.NutriApp.NutriApp.repository.AuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorityService {

    private final AuthorityRepository authorityRepository;

    public void insertar (Authority authority) throws AuthorityInvalidaException {
        if (authorityRepository.existsById(authority.getId())){
            throw new AuthorityInvalidaException("El authority ya existe con el id = " +authority.getId());
        }

        authorityRepository.save(authority);
    }


    @PreAuthorize("hasRole('ADMIN')")   //le agrego seguridad al service por las dudas
    public void cambiaRol_A_ADMIN (String username) throws AuthorityInvalidaException{
        Optional<Authority> optionalAuthority = authorityRepository.findByUsuarioUsername(username);

        if (optionalAuthority.isEmpty()){
            throw new AuthorityInvalidaException("El authority no existe con el username = " +username);
        }


        if (optionalAuthority.get().getAuthority().equals(Role.ROLE_ADMIN)) {
            throw new AuthorityInvalidaException("El authority ya tiene el rol al cual desea cambiar");
        }

        authorityRepository.cambiarRol_A_ADMIN(username);
    }

    @PreAuthorize("hasRole('ADMIN')")   //le agrego seguridad al service por las dudas
    public void cambiaRol_A_CLIENTE (String username) throws AuthorityInvalidaException{
        Optional<Authority> optionalAuthority = authorityRepository.findByUsuarioUsername(username);

        if (optionalAuthority.isEmpty()){
            throw new AuthorityInvalidaException("El authority no existe con el username = " +username);
        }


        if (optionalAuthority.get().getAuthority().equals(Role.ROLE_CLIENT)) {
            throw new AuthorityInvalidaException("El authority ya tiene el rol al cual desea cambiar");
        }

        authorityRepository.cambiarRol_A_CLIENTE(username);
    }

    public Authority obtenerRolLogeado () throws AuthorityInvalidaException{
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogeado =  (Usuario) auth.getPrincipal();

        return authorityRepository.findByUsuarioUsername(usuarioLogeado.getUsername())
                .orElseThrow(() -> new AuthorityInvalidaException("El authority con el username = " +usuarioLogeado.getUsername()+ " no existe"));

    }
}
