package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.exceptions.DiaInvalidoException;
import com.NutriApp.NutriApp.mapper.DiaMapper;
import com.NutriApp.NutriApp.modelo.*;
import com.NutriApp.NutriApp.modelo.dto.DiaDTO;
import com.NutriApp.NutriApp.modelo.dto.EstadoDiaDTO;
import com.NutriApp.NutriApp.modelo.enums.EstadoDia;
import com.NutriApp.NutriApp.repository.DiaRepository;
import com.NutriApp.NutriApp.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaService {

    private final DiaRepository diaRepository;
    private final UsuarioService usuarioService;

    // variable de tolerancia calorica para determinar el estado nutricional de un dia teniendo en cuenta
    // que se puede pasar por pocas calorias e igualmente se lo tomamos como objetivo diario cumplido
    private static final double TOLERANCIA_CALORICA = 50;


    // Crear nuevo dia
    public void guardar(Dia dia) {

        diaRepository.save(dia);
    }


    // Devuelve un día por fecha, o lo crea si no existe
    public Dia obtenerODiaOCrear(LocalDate fecha) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        return diaRepository.findByFechaAndUsuario(fecha, user)
                .orElseGet(() -> {
                    Dia nuevoDia = new Dia();
                    nuevoDia.setFecha(fecha);
                    nuevoDia.setUsuario(user);

                    //ponemos el estado del dia en pendiente porque cuando se crea las calorias
                    //consumidas van a ser 0
                    nuevoDia.setEstadoDia(EstadoDia.PENDIENTE);

                    // Inicializar la hidratación
                    Hidratacion nuevaHidratacion = new Hidratacion();
                    nuevaHidratacion.setCantidadMl(0); // Valor inicial
                    nuevaHidratacion.setDia(nuevoDia); // Vínculo bidireccional obligatorio

                    nuevoDia.setHidratacion(nuevaHidratacion); // Asignar al día

                    guardar(nuevoDia);
                    return nuevoDia;
                });
    }

    // Obtener un día por fecha
    public Optional<Dia> obtenerDiaPorFecha(LocalDate fecha, Usuario usuario) {
        return diaRepository.findByFechaAndUsuario(fecha, usuario);
    }


    public DiaDTO verDiaCompleto(LocalDate fecha) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        Dia dia = obtenerODiaOCrear(fecha);

        // Convertimos la entidad a DTO
        return DiaMapper.toDiaDTO(dia);
    }


    public List<Dia> verHistorialDias() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();
        Usuario usuarioConDias = usuarioService.obtenerUsuarioConDias(user.getUsername());

        List<Dia> dias = usuarioConDias.getDias();

        if (dias.isEmpty()) {
            throw new DiaInvalidoException("El usuario todavia no tiene dias cargados ");
        }

        return dias;

    }

    public void caloriasRestantesDia(LocalDate fecha) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();
        double objetivoDiario = user.getPerfilNutricional().getObjetivoDiario();
        Optional<Dia> dia = diaRepository.findByFechaAndUsuario(fecha, user);
        if (dia.isEmpty()) {
            throw new DiaInvalidoException("No existe un día cargado para ese usuario");
        }

        Dia diaActual = dia.get();

        List<ActividadFisica> actividadesFisicas = diaActual.getActividadesFisicasRealizadas();
        if (actividadesFisicas != null && !actividadesFisicas.isEmpty()) {

            for (ActividadFisica actividad : actividadesFisicas) {
                objetivoDiario += actividad.getCaloriasGastadas();
            }
        }

        List<ComidaIngerida> comidasIngeridas = diaActual.getComidasIngeridas();
        double caloriasConsumidas = 0;
        if (comidasIngeridas != null && !comidasIngeridas.isEmpty()) {
            for (ComidaIngerida comida : comidasIngeridas) {
                caloriasConsumidas += comida.getCalorias();
            }
        }

        double caloriasRestantes = objetivoDiario - caloriasConsumidas;

        diaActual.setCaloriasRestantes(caloriasRestantes);

        diaActual.setEstadoDia(calcularEstadoDia(caloriasConsumidas, objetivoDiario));

        guardar(diaActual);
    }

    public double verCaloriasConsumidasDeunDia( LocalDate fecha) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        // Buscar el día correspondiente
        Optional<Dia> diaEncontrado = obtenerDiaPorFecha(fecha, user);
        if (diaEncontrado.isEmpty()) {
            throw new DiaInvalidoException("No se encontro el dia registrado con fecha: " + fecha);
        }

        // Extraer la lista de comidas ingeridas
        List<ComidaIngerida> comidas = diaEncontrado.get().getComidasIngeridas();

        // Sumar las calorías de las comidas ingeridas
        double totalCalorias = comidas.stream()
                .mapToDouble(ComidaIngerida::getCalorias)
                .sum();

        return totalCalorias;
    }

    private EstadoDia calcularEstadoDia(double caloriasConsumidas, double objetivoDiario) {

        if (caloriasConsumidas < objetivoDiario - TOLERANCIA_CALORICA) {
            return EstadoDia.PENDIENTE;
        }

        if (caloriasConsumidas > objetivoDiario + TOLERANCIA_CALORICA) {
            return EstadoDia.EXCEDIDO;
        }

        return EstadoDia.CUMPLIDO;
    }


    public List<EstadoDiaDTO> obtenerEstadosDelMes(int año, int mes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        //Creamos una fecha utilizando el año y mes que recibimos, y ponemos como día el primer dia del mes
        LocalDate fechaInicio = LocalDate.of(año, mes, 1);

        //obtenemos una fecha que esta al final del mes que se paso
        LocalDate fechaFin = fechaInicio.withDayOfMonth(
                fechaInicio.lengthOfMonth()
        );

        // se busca todos los dias que esten asociados con el usuario y esten entre el
        // primer día hasta el último día del mes que se paso en el frontend
        List<Dia> dias = diaRepository.findByUsuarioAndFechaBetween(
                user,
                fechaInicio,
                fechaFin
        );


        // transformamos la lista de dias en un stream para poder trasnformar cada entidad de dia
        // con todos los atributos que tiene en su simple DTO
        return dias.stream()
                .map(dia -> {

                    // Calcular calorías consumidas
                    double caloriasConsumidas = 0;

                    List<ComidaIngerida> comidasIngeridas =
                            dia.getComidasIngeridas();

                    if (comidasIngeridas != null && !comidasIngeridas.isEmpty()) {

                        caloriasConsumidas = comidasIngeridas.stream()
                                .mapToDouble(ComidaIngerida::getCalorias)
                                .sum();
                    }


                    // Objetivo calórico base del usuario
                    double objetivoCalorico = user.getPerfilNutricional()
                            .getObjetivoDiario();

                    // Se agregan las calorías gastadas mediante actividad física
                    if (dia.getActividadesFisicasRealizadas() != null) {

                        objetivoCalorico += dia.getActividadesFisicasRealizadas()
                                .stream()
                                .mapToDouble(ActividadFisica::getCaloriasGastadas)
                                .sum();
                    }

                    return new EstadoDiaDTO(
                            dia.getFecha(),
                            dia.getEstadoDia(),
                            caloriasConsumidas,
                            objetivoCalorico
                    );
                })
                .toList();
    }

}

