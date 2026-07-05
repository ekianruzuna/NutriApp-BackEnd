package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.modelo.Logros.HistorialLogro;
import com.NutriApp.NutriApp.modelo.Logros.Logro;
import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.enums.TipoLogro;
import com.NutriApp.NutriApp.repository.HistorialLogroRepository;
import com.NutriApp.NutriApp.repository.LogroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class LogroService {

    private final DiaService diaService;
    private final LogroRepository logroRepository;
    private final HistorialLogroRepository historialLogroRepository;

    //comprueba si esta en condiciones de otorgar el logro en una fehca determinada
    public void comprobarYGuardarMetaCaloricaDiaria(LocalDate date){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();

        //evita duplicado buscando, el mismo tipo de logro que tiene un usuario en una fecha
        if (historialLogroRepository.existsByUsuarioUsernameAndLogroObtenido_TipoLogroAndFechaObtencion(user.getUsername(), TipoLogro.META_CALORICA_DIARIA, date)){
            return;
        }

        //obtenemos las calorias restantes en el dia
        double caloriasRestantes = diaService.obtenerODiaOCrear(date).getCaloriasRestantes();

        //si no quedan calorias que consumir persistimos el logro
        if (caloriasRestantes < 0){

            //buscamos el logro en la BDD
            Logro logro = logroRepository.findByTipoLogro(TipoLogro.META_CALORICA_DIARIA).orElseThrow();

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

    //comprueba que al eliminar un alimento si ese alimento hace que no cumpla el logro
    public void comprobarYEliminarMetaCaloricaDiaria(LocalDate date){

    }

    //busca y retorna la cantidad de veces que gano un tipo de logro
    public int obtenerCantidadVecesLogroObtenido(TipoLogro tipoLogro){
        return 1;
    }

    //obtener el historial completo de logros para mostrarlo en el front
    public HistorialLogro obtenerHistorialLogros(){
        return new HistorialLogro();
    }


    //se podria implementar el tema de una interfaz evaluator para que evalue todas las clases que la implementen y asi tener un solo metodo que compruebe y
    // guarde y que compruebe y elimine el logro. Despues me gustaria agregar un atributo de fecha del logro, asi queda separada la fecha que se obtuvo el
    // logro y la fecha a la que pertenece el logro

}
