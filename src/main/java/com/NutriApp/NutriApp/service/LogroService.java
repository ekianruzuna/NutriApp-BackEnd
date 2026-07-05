package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.exceptions.LogroInvalidoException;
import com.NutriApp.NutriApp.modelo.Logros.HistorialLogro;
import com.NutriApp.NutriApp.modelo.Logros.Logro;
import com.NutriApp.NutriApp.modelo.Logros.LogroEvaluator;
import com.NutriApp.NutriApp.modelo.Logros.MetaDiariaEvaluator;
import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.enums.TipoLogro;
import com.NutriApp.NutriApp.repository.HistorialLogroRepository;
import com.NutriApp.NutriApp.repository.LogroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class LogroService {

    private final LogroRepository logroRepository;
    private final HistorialLogroRepository historialLogroRepository;
    private final List<LogroEvaluator> evaluadores;

    //comprueba que logros estan en condiciones de otorgarse en una fecha
    public void comprobarYGuardarTodosLosLogros(LocalDate date){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();


        //recorremos todos los evaluadores que implementan la interfaz evaluadora asi podemos acceder al metodo seCumple() de cada uno
        //donde se encuentra la logica de complimiento de cada logro (es muy parecido a una clase abstracta con herencia solo que aca no hacia falta una clase abstracta)
        for (LogroEvaluator logroEvaluator : evaluadores){

            //evita duplicado buscando, el mismo tipo de logro que tiene un usuario en una fecha
            if (historialLogroRepository.existsByUsuarioUsernameAndLogroObtenido_TipoLogroAndFechaObtencion(user.getUsername(), logroEvaluator.getTipo(), date)){
                continue;   //en un for la instruccion 'continue' hace que salte al siguiente directamente y se salta todo lo de abajo
            }

            //si no se cumple la condicion para otrogar el logro
            if (!logroEvaluator.seCumple(date, user)){
                continue;   //seguinmos con el siguiente evaluador
            }

            //buscamos el logro en la BDD
            Logro logro = logroRepository.findByTipoLogro(logroEvaluator.getTipo()).orElseThrow();

            //creamos el evento del historial
            HistorialLogro historialLogro = HistorialLogro.builder()
                    .usuario(user)
                    .logroObtenido(logro)
                    .fechaObtencion(date)
                    .build();

            //persistimos el logro
            historialLogroRepository.save(historialLogro);
        }
    }

    //comprueba que logros estan en condiciones de otorgarse en una fecha
    public void comprobarYGuerdarLogro(LocalDate date, TipoLogro tipoLogro){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        //evita duplicado buscando, el mismo tipo de logro que tiene un usuario en una fecha
        if (historialLogroRepository.existsByUsuarioUsernameAndLogroObtenido_TipoLogroAndFechaObtencion(user.getUsername(), tipoLogro, date)){
            return;
        }

        //Obtenemos el evualuador del logro que se paso por parametro
        LogroEvaluator logroEvaluator = obtenerEvaluador(tipoLogro);

        //si no se cumple la condicion para otrogar el logro
        if (!logroEvaluator.seCumple(date, user)){
            return;
        }

        //buscamos el logro en la BDD
        Logro logro = logroRepository.findByTipoLogro(tipoLogro).orElseThrow();

        //creamos el evento del historial
        HistorialLogro historialLogro = HistorialLogro.builder()
                .usuario(user)
                .logroObtenido(logro)
                .fechaObtencion(date)
                .build();

        //persistimos el logro
        historialLogroRepository.save(historialLogro);
    }

    //comprueba que al eliminar un alimento si ese alimento hace que no cumpla el logro
    public void comprobarYEliminarLogro(LocalDate date, TipoLogro tipoLogro){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        //buscamos el logro en el historial
        Optional<HistorialLogro> logro = historialLogroRepository.findByUsuarioUsernameAndLogroObtenido_TipoLogroAndFechaObtencion(user.getUsername(), tipoLogro, date);

        //si no existe no hacemos nada
        if (logro.isEmpty()){
            return;
        }

        //Obtenemos el evualuador del logro que se paso por parametro
        LogroEvaluator logroEvaluator = obtenerEvaluador(tipoLogro);

        //si se cumple la condicion todavia, significa que todavia tiene el logro
        if (logroEvaluator.seCumple(date, user)){
            return;
        }

        //eliminamos el logro en el historial
        historialLogroRepository.delete(logro.get());
    }

    //busca y retorna la cantidad de veces que gano un tipo de logro
    public long obtenerCantidadVecesLogroObtenido(TipoLogro tipoLogro){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        //retonamo la cantidad de logros que obtuvo un usuario en base a un tipo de logro que se pasa por parametro
        return historialLogroRepository.countAllByUsuario_UsernameAndLogroObtenido_TipoLogro(user.getUsername(), tipoLogro);
    }

    //obtener el historial completo de logros para mostrarlo en el front
    public List<HistorialLogro> obtenerHistorialLogros(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        //obtenemos el historial
        List<HistorialLogro> historialLogros = historialLogroRepository.findAllByUsuarioUsername(user.getUsername());

        //si la lista esta vacia tiramos exception
        if (historialLogros.isEmpty()){
            throw new LogroInvalidoException("El usuario '" + user.getUsername() + "' no tiene ningun logro");
        }

        //retornamos la lista
        return historialLogros;
    }

    //metodo que busca cual es la condicion para evaluar si se cumple el logro o no
    private LogroEvaluator obtenerEvaluador(TipoLogro tipoLogro){
        //Obtenemos el evualuador del logro que se paso por parametro
        return evaluadores.stream()
                .filter(x -> x.getTipo() == tipoLogro)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe el evaluador para :" + tipoLogro));
    }



    // Despues me gustaria agregar un atributo de fecha del logro, asi queda separada la fecha que se obtuvo el
    // logro y la fecha a la que pertenece el logro
}
