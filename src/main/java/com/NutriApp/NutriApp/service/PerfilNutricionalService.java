package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.modelo.dto.PerfilNutricionalDTO;
import com.NutriApp.NutriApp.modelo.PerfilNutricional;
import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.enums.Genero;
import com.NutriApp.NutriApp.repository.PerfilNutricionalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PerfilNutricionalService {

    // No es necesario usar @Autowired para inyectar la instancia de PersonaRepository,
    // ya que Spring detecta que esta clase (PersonaService) tiene un único constructor
    // que recibe como parámetro un bean (PersonaService) (aca lo especificamos con @RequierdArgsContructor).
    // Spring automáticamente realiza la inyección de dependencias usando ese constructor.
    // Si existieran múltiples constructores, Spring no sabría cuál usar y se necesitaría
    // especificar la inyección de otra manera (por ejemplo, con @Autowired).
    private final PerfilNutricionalRepository perfilNutricionalRepository;

    public PerfilNutricional realizar_calculo_BMR(PerfilNutricionalDTO dto, Genero genero) {
        PerfilNutricional objeto = new PerfilNutricional();
        objeto.setPeso(dto.getPeso());
        objeto.setAltura(dto.getAltura());
        objeto.setNivelActividadFisica(dto.getNivelActividadFisica());
        objeto.setEdad(dto.getEdad());
        objeto.setObjetivoCaloricoTipo(dto.getObjetivoCaloricoTipo());

        Double tmb; // Tasa Metabólica Basal

        if (genero == Genero.MASCULINO) {
            // TMB para hombres: (10 * peso en kg) + (6.25 * altura en cm) - (5 * edad en años) + 5
            tmb = (10 * objeto.getPeso()) + (6.25 * objeto.getAltura()) - (5 * objeto.getEdad()) + 5;
        } else { // Asumimos Genero.FEMENINO
            // TMB para mujeres: (10 * peso en kg) + (6.25 * altura en cm) - (5 * edad en años) - 161
            tmb = (10 * objeto.getPeso()) + (6.25 * objeto.getAltura()) - (5 * objeto.getEdad()) - 161;
        }

        // Calcular el Gasto Energético Total (GET) usando la TMB y el nivel de actividad
        objeto.setGEB(tmb * objeto.getNivelActividadFisica().getFactor());

        // Calcular el objetivo diario final del usuario
        objeto.setObjetivoDiario(objeto.getGEB() + objeto.getGEB() * objeto.getObjetivoCaloricoTipo().getAjustePorcentaje());

        return objeto;
    }

    // Crear perfil nutricional del usuario
    public void guardar(PerfilNutricional perfilNutricional) {

        perfilNutricionalRepository.save(perfilNutricional);
    }

    //Obtenes tu perfil nutricional
    public PerfilNutricional obtenerPerfilNutricional ()
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = (Usuario) auth.getPrincipal();
        return usuario.getPerfilNutricional();
    }

    @Transactional
    public PerfilNutricional actualizarPerfilNutricional(PerfilNutricionalDTO perfilDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();
        PerfilNutricional perfilExistente = user.getPerfilNutricional();

        if (perfilExistente != null) {
            PerfilNutricional perfilActualizado = realizar_calculo_BMR(perfilDTO, user.getPersona().getGenero());
            perfilExistente.setGEB(perfilActualizado.getGEB());
            perfilExistente.setObjetivoDiario(perfilActualizado.getObjetivoDiario());
            perfilExistente.setNivelActividadFisica(perfilActualizado.getNivelActividadFisica());
            perfilExistente.setAltura(perfilActualizado.getAltura());
            perfilExistente.setPeso(perfilActualizado.getPeso());
            perfilExistente.setEdad(perfilActualizado.getEdad());
            perfilExistente.setObjetivoCaloricoTipo(perfilActualizado.getObjetivoCaloricoTipo());

            guardar(perfilExistente);
        } else {
            PerfilNutricional nuevoPerfil = realizar_calculo_BMR(perfilDTO, user.getPersona().getGenero());
            user.setPerfilNutricional(nuevoPerfil);
            guardar(nuevoPerfil);

            return  nuevoPerfil;
        }

        return perfilExistente;

    }



}
