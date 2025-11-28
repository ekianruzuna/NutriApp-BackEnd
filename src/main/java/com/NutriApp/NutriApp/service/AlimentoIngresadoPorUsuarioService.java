package com.NutriApp.NutriApp.service;


import com.NutriApp.NutriApp.exceptions.AlimentoInvalidoException;
import com.NutriApp.NutriApp.exceptions.AlimetoIngreadoPorElUsuarioException;
import com.NutriApp.NutriApp.modelo.AlimentoIngresadoPorUsuario;
import com.NutriApp.NutriApp.modelo.SolicitudAltaAlimento;
import com.NutriApp.NutriApp.modelo.dto.AlimentoBusquedaDTO;
import com.NutriApp.NutriApp.modelo.dto.MacronutrienteDTO;
import com.NutriApp.NutriApp.repository.AlimentoIngresadoPorUsuarioRepository;
import com.NutriApp.NutriApp.repository.SolicitudRespository;
import jakarta.transaction.Transactional;
import org.apache.catalina.LifecycleState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import java.util.List;
import java.util.Optional;

@Service
public class AlimentoIngresadoPorUsuarioService {

    @Autowired
    private AlimentoIngresadoPorUsuarioRepository alimentoRepository;
    @Autowired
    private SolicitudRespository solicitudRespository;
    private TransactionalOperator transactionalOperator;

    public boolean existsByNombre (String nombre){
        return alimentoRepository.existsByNombreComidaIgnoreCase(nombre);
    }

    public boolean existsById (long id){
        return alimentoRepository.existsById(id);
    }

    @Transactional
    public AlimentoIngresadoPorUsuario insertar (AlimentoIngresadoPorUsuario alimentoIngresadoPorUsuario) throws AlimetoIngreadoPorElUsuarioException{
        //validacion
        if (alimentoRepository.existsByNombreComida(alimentoIngresadoPorUsuario.getNombreComida())){
            throw new AlimetoIngreadoPorElUsuarioException("El alimento ya existe con el nombre: " + alimentoIngresadoPorUsuario.getNombreComida());
        }

        //guardamos
        alimentoRepository.save(alimentoIngresadoPorUsuario);

        return alimentoIngresadoPorUsuario;
    }

    @Transactional
    public AlimentoIngresadoPorUsuario editar (long idAlimentoEditar, AlimentoIngresadoPorUsuario alimentoNuevo) throws AlimetoIngreadoPorElUsuarioException{
        Optional<AlimentoIngresadoPorUsuario> alimento = alimentoRepository.findById(idAlimentoEditar);

        if (alimento.isEmpty()){
            throw new AlimetoIngreadoPorElUsuarioException("El alimento con el id: '" + idAlimentoEditar + "' no existe");
        }

        //si cambio el nombre y ya existe ese nombre
        if (!alimento.get().getNombreComida().equals(alimentoNuevo.getNombreComida())){
            if (alimentoRepository.existsByNombreComida(alimentoNuevo.getNombreComida())){
                throw new AlimetoIngreadoPorElUsuarioException("El alimento ya existe con el nombre: " + alimentoNuevo.getNombreComida());
            }
        }

        //actualizamos los datos
        alimento.get().setNombreComida(alimentoNuevo.getNombreComida());
        alimento.get().setGramosPorPorcion(alimentoNuevo.getGramosPorPorcion());
        alimento.get().setCalorias(alimentoNuevo.getCalorias());
        alimento.get().setProteinas(alimentoNuevo.getProteinas());
        alimento.get().setCarbohidratos(alimentoNuevo.getCarbohidratos());
        alimento.get().setGrasas(alimentoNuevo.getGrasas());

        //guardamos
        alimentoRepository.save(alimento.get());

        return alimento.get();
    }

    @Transactional
    public String eliminar (long idAlimentoEliminar) throws AlimetoIngreadoPorElUsuarioException{
        if (!alimentoRepository.existsById(idAlimentoEliminar)){
            throw new AlimetoIngreadoPorElUsuarioException("El alimento con el id: '" + idAlimentoEliminar+ "' no existe");
        }

        alimentoRepository.deleteById(idAlimentoEliminar);

        return "Alimento eliminado correctamente";
    }

    @Transactional
    public void insertarBasandoseEnSolicitud (SolicitudAltaAlimento solicitudAltaAlimento) throws AlimetoIngreadoPorElUsuarioException {
        //Creamos el alimneto en base a una solicitud
        AlimentoIngresadoPorUsuario alimento = AlimentoIngresadoPorUsuario.builder()
                .nombreComida(solicitudAltaAlimento.getNombreComida())
                .gramosPorPorcion(solicitudAltaAlimento.getPorcion())
                .calorias(solicitudAltaAlimento.getCalorias())
                .proteinas(solicitudAltaAlimento.getProteinas())
                .grasas(solicitudAltaAlimento.getGrasas())
                .carbohidratos(solicitudAltaAlimento.getCarbohidratos())
                .build();

        //validacion extra por las dudas (se supone que no se puede cargar una solicitud con algun dato nulo)
        if (!alimento.esValido()){
            throw new AlimetoIngreadoPorElUsuarioException("Datos nulos");
        }

        //se guarda en la bdd el alimento
        alimentoRepository.save(alimento);
    }

    public Optional<MacronutrienteDTO> obtenerMacronutrientes(String nombreComida, Long id) {
        Optional <MacronutrienteDTO> dto = alimentoRepository.findMacronutrientesByNombreComidaAndId(nombreComida, id);
        return dto;
    }

    //lista todos los aliemntos de nuestra base de datos
    @PreAuthorize("hasRole('ADMIN')")   //validacion extra por si las dudas
    public List<AlimentoIngresadoPorUsuario> listarTodos () throws AlimetoIngreadoPorElUsuarioException{
        //obtenemos todos los alimentos
        List<AlimentoIngresadoPorUsuario> lista = alimentoRepository.findAll();

        if (lista.isEmpty()){
            throw new AlimetoIngreadoPorElUsuarioException("No hay ningun alimento cargado en la bdd");
        }

        return lista;
    }

    //lista los ultimos 10 aliemntos de nuestra base de datos
    @PreAuthorize("hasRole('ADMIN')")   //validacion extra por si las dudas
    public List<AlimentoIngresadoPorUsuario> listarUtimos10 () throws AlimetoIngreadoPorElUsuarioException{
        //obtenemos todos los alimentos
        Optional<List<AlimentoIngresadoPorUsuario>> lista = alimentoRepository.findTop10ByOrderByIdDesc();

        if (lista.isEmpty()){
            throw new AlimetoIngreadoPorElUsuarioException("No hay ningun alimento cargado en la bdd");
        }

        return lista.get();
    }

    //filtra los alimentos de nuestra bdd buscando por matcheos
    public List<AlimentoIngresadoPorUsuario> filtrarAlimentosPorNombreComida (String nombreComida) throws AlimetoIngreadoPorElUsuarioException{
        //buscamos los que coincidan
        Optional<List<AlimentoIngresadoPorUsuario>> lista = alimentoRepository.findAllByNombreComidaContainingIgnoreCase(nombreComida);

        if (lista.isEmpty()){
            throw new AlimetoIngreadoPorElUsuarioException("Alimento no encontrado con el nombre = " + nombreComida);
        }

        return lista.get();
    }

    //retorna una lista sin comprobrar que se encuentre el aliemtno porque este metodo se utiliza para unificar los alimentos de la api con los de nuestra BDD
    public List<AlimentoIngresadoPorUsuario> filtrarAlimentosPorNombreComidaSinException (String nombreComida) {
        //buscamos los que coincidan
        Optional<List<AlimentoIngresadoPorUsuario>> lista = alimentoRepository.findAllByNombreComidaContainingIgnoreCase(nombreComida);

        return lista.get();
    }

    //convierte la lista de alimentos de nuestra BDD a una lista de AliemntoBusquedaDTO para que sea igual que en la api
    public List<AlimentoBusquedaDTO> convertirListaDTO (List<AlimentoIngresadoPorUsuario> alimentos){
        return alimentos.stream()
                .map(AlimentoBusquedaDTO::from)
                .toList();
    }

//    @PreAuthorize("hasRole('ADMIN')")
//    public String eliminarAlimento (String nombreComida) throws AlimetoIngreadoPorElUsuarioException{
//
//        //obtenemos el alimento
//        Optional<AlimentoIngresadoPorUsuario> alimento = alimentoRepository.findByNombreComidaIgnoreCase(nombreComida);
//
//        //validamos
//        if (alimento.isEmpty()){
//            throw new AlimentoInvalidoException("No se encontro el alimento con el nombre = " + nombreComida);
//        }
//
//        //borramos el alimento
//        alimentoRepository.de
//
//    }

}
