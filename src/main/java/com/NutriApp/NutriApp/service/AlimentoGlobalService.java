package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.exceptions.AlimentoInvalidoException;
import com.NutriApp.NutriApp.exceptions.AlimetoIngreadoPorElUsuarioException;
import com.NutriApp.NutriApp.modelo.AlimentoIngresadoPorUsuario;
import com.NutriApp.NutriApp.modelo.dto.AlimentoBusquedaDTO;
import com.NutriApp.NutriApp.repository.AlimentoIngresadoPorUsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


//Service para englobar los alimentos de la api y los de nuestra BDD
@Service
public class AlimentoGlobalService {

    @Autowired
    private AlimentoIngresadoPorUsuarioService alimentoIngresadoPorUsuarioService;

    @Autowired
    private FoodDataService foodDataService;

    @Autowired
    private NutricionService nutricionService;


    //lista alimentos combinando de la api con los de nuestra BDD
    @Transactional
    public List<AlimentoBusquedaDTO> filtrarAlimentosCombinadosPorNombreComida(String nombreComida) throws Exception {
        // Obtenemos los alimentos de nuestra BDD
        List<AlimentoBusquedaDTO> alimentosBDD = alimentoIngresadoPorUsuarioService
                .convertirListaDTO(
                        alimentoIngresadoPorUsuarioService.filtrarAlimentosPorNombreComidaSinException(nombreComida)
                );

        // Obtenemos los alimentos de la API externa
        List<AlimentoBusquedaDTO> alimentosAPI = foodDataService.buscarAlimentosPorNombreSinException(nombreComida);

        // Creamos la lista unificada
        List<AlimentoBusquedaDTO> listaUnificada = new ArrayList<>();
        if (alimentosBDD != null) listaUnificada.addAll(alimentosBDD);
        if (alimentosAPI != null) listaUnificada.addAll(alimentosAPI);

        // Opcional: log si no hay resultados
        if (listaUnificada.isEmpty()) {
            System.out.println("No se encontró ninguna comida con el nombre = " + nombreComida);
            // No lanzamos excepción, devolvemos lista vacía
        }

        return listaUnificada;
    }

}
