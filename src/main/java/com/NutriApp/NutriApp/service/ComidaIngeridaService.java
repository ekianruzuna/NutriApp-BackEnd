package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.exceptions.ActividadFisicaInvalidaException;
import com.NutriApp.NutriApp.modelo.*;
import com.NutriApp.NutriApp.modelo.dto.ComidaIngeridaDTO;
import com.NutriApp.NutriApp.modelo.dto.MacronutrienteDTO;
import com.NutriApp.NutriApp.modelo.dto.ModificarComidaIngeridaDTO;
import com.NutriApp.NutriApp.exceptions.ComidaIngeridaException;
import com.NutriApp.NutriApp.exceptions.DiaInvalidoException;
import com.NutriApp.NutriApp.modelo.enums.TipoComida;
import com.NutriApp.NutriApp.repository.ComidaIngeridaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComidaIngeridaService {

    private final ComidaIngeridaRepository comidaIngeridaRepository;
    private final FoodDataService foodDataService;
    private final NutricionService nutricionService;
    private final DiaService diaService;
    private final AlimentoIngresadoPorUsuarioService alimentoIngresadoPorUsuarioService;

    @Transactional
    public void agregarComidaIngerida(long id_comida, String nombre, double gramos, TipoComida tipo, LocalDate fecha) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        // Buscás el Día o lo creás si no existe
        Dia dia = diaService.obtenerODiaOCrear(fecha);

        // Buscás si ya existe una comida igual ese día
        Optional<ComidaIngerida> comidaExistente = comidaIngeridaRepository
                .findByUserIdAndFechaAndComidaIdAndTipoComida(user.getPersona().getId(), fecha, id_comida, tipo);

        if (comidaExistente.isPresent()) {
            modificarComida(new ModificarComidaIngeridaDTO(
                    id_comida,
                    nombre,
                    gramos + comidaExistente.get().getCantidad(),
                    comidaExistente.get().getTipoComida(),
                    tipo,
                    fecha
            ));
        } else {
            ComidaIngerida comida = convertir_comidaid(new ComidaIngerida(), nombre, id_comida, gramos);
            comida.setTipoComida(tipo);
            dia.getComidasIngeridas().add(comida);
            comida.setDia(dia);
            guardar(comida);
        }

        diaService.caloriasRestantesDia(fecha);
    }

    public ComidaIngerida convertir_comidaid(ComidaIngerida comidaIngerida, String nombre, long comida_id, double gramos) throws Exception {

        MacronutrienteDTO dto = new MacronutrienteDTO();

        Optional<MacronutrienteDTO> optionalMacronutrienteDTO = alimentoIngresadoPorUsuarioService.obtenerMacronutrientes(nombre, comida_id);

        if (optionalMacronutrienteDTO.isEmpty()) {
            optionalMacronutrienteDTO = nutricionService.obtenerMacronutrientesPorId(comida_id);
        }

        if (!optionalMacronutrienteDTO.isEmpty() && optionalMacronutrienteDTO.get().getNombreComida().equals(nombre)) {
            dto = optionalMacronutrienteDTO.get();
        } else {
            throw new ComidaIngeridaException("No se encontro la comida con nombre: " + nombre);
        }

        if (dto.getGramosPorPorcion() == 0) {
            throw new ComidaIngeridaException("El valor de gramosPorPorcion no puede ser nulo ni cero.");
        }

        comidaIngerida.setNombreComida(dto.getNombreComida());
        comidaIngerida.setIdComidaApi(dto.getId_comida());
        return settearComidaIngerida(comidaIngerida, gramos, dto.getCalorias(), dto.getProteinas(), dto.getGrasas(), dto.getCarbohidratos(), dto.getGramosPorPorcion());
    }

    // Crear nuevo dia
    public void guardar(ComidaIngerida comidaIngerida) {

        comidaIngeridaRepository.save(comidaIngerida);
    }

    //modificar algun alimento dentro de la base de datos
    public void modificarComida(ModificarComidaIngeridaDTO dto) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();
        Optional<Dia> dia = diaService.obtenerDiaPorFecha(dto.getFecha(), user);
        if (dia.isEmpty()) {
            throw new DiaInvalidoException("No hay un registro realizado en ese dia");
        }

        if (dto.getTipoComida() != dto.getTipoComidaNuevo()) {

            ComidaIngerida modificar = buscarComidaIngerida(dto.getFecha(), dto.getId(), dto.getTipoComida());
            Optional<ComidaIngerida> modificar2 = buscarOptionalComidaIngerida(dto.getFecha(), dto.getId(), dto.getTipoComidaNuevo());
            if (modificar2.isEmpty()) {
                modificar.setTipoComida(dto.getTipoComidaNuevo());
                modificar = convertir_comidaid(modificar, dto.getNombre(), modificar.getIdComidaApi(), (dto.getGramos()));
                guardar(modificar);
            } else {
                ComidaIngerida modificarFinal = convertir_comidaid(modificar2.get(), modificar2.get().getNombreComida(), modificar2.get().getIdComidaApi(), (modificar2.get().getCantidad() + dto.getGramos()));
                comidaIngeridaRepository.delete(modificar);
                guardar(modificarFinal);
            }
        } else {
            ComidaIngerida modificar = buscarComidaIngerida(dto.getFecha(), dto.getId(), dto.getTipoComida());
            modificar = convertir_comidaid(modificar, dto.getNombre(), modificar.getIdComidaApi(), (dto.getGramos()));
            guardar(modificar);
        }
        diaService.caloriasRestantesDia(dia.get().getFecha());
    }

    // metodo que settea ciertos valores de la comida para modularizar codigo
    public ComidaIngerida settearComidaIngerida(ComidaIngerida comidaIngerida, double gramos, double calorias, double proteinas, double grasas, double carbohidratos, double gramosPorPorcion) {
        // armamos el DTO
        comidaIngerida.setCalorias((gramos * calorias) / gramosPorPorcion);
        comidaIngerida.setProteinas((gramos * proteinas) / gramosPorPorcion);
        comidaIngerida.setGrasas((gramos * grasas) / gramosPorPorcion);
        comidaIngerida.setCarbohidratos((gramos * carbohidratos) / gramosPorPorcion);
        comidaIngerida.setCantidad(gramos);
        return comidaIngerida;
    }

    public double verCaloriasConsumidasDeunDia(@RequestParam LocalDate fecha) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        // Buscar el día correspondiente
        Optional<Dia> diaEncontrado = diaService.obtenerDiaPorFecha(fecha, user);
        if (diaEncontrado.isEmpty()) {
            throw new DiaInvalidoException("No se encontro el dia registrado con fecha: " + fecha);
        }

        // Extraer la lista de comidas ingeridas
        List<ComidaIngerida> comidas = diaEncontrado.get().getComidasIngeridas();

        // Sumar las calorías de las comidas ingeridas
        double totalCalorias = comidas.stream()
                .mapToDouble(ComidaIngerida::getCantidad)
                .sum();

        return totalCalorias;
    }

    public ComidaIngerida buscarComidaIngerida(LocalDate fecha, long comidaId, TipoComida tipoComida) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        return comidaIngeridaRepository.findByUserIdAndFechaAndComidaIdAndTipoComida(user.getPersona().getId(), fecha, comidaId, tipoComida)
                .orElseThrow(() -> new ComidaIngeridaException("No se encontró una comida para el día con la fecha: " + fecha + " en el/la " + tipoComida));
    }

    public Optional<ComidaIngerida> buscarOptionalComidaIngerida(LocalDate fecha, long comidaId, TipoComida tipoComida) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        return comidaIngeridaRepository.findByUserIdAndFechaAndComidaIdAndTipoComida(
                user.getPersona().getId(),
                fecha,
                comidaId,
                tipoComida
        );
    }

    public void eliminarComidaIngerida(LocalDate fecha, long comidaId, TipoComida tipoComida) {
        ComidaIngerida comida = buscarComidaIngerida(fecha, comidaId, tipoComida);

        Dia dia = comida.getDia();
        dia.getComidasIngeridas().remove(comida);

        comidaIngeridaRepository.delete(comida);  // esto lo elimina de la DB
        diaService.caloriasRestantesDia(fecha);   // recalculás con la lista actualizada
    }

}
