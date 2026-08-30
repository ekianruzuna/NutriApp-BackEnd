package com.NutriApp.NutriApp.service;


import com.NutriApp.NutriApp.exceptions.SolicitudInvalidaException;
import com.NutriApp.NutriApp.modelo.Persona;
import com.NutriApp.NutriApp.modelo.SolicitudAltaAlimento;
import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.dto.AlimentoBusquedaDTO;
import com.NutriApp.NutriApp.repository.SolicitudRespository;
import com.NutriApp.NutriApp.service.Mail.MailService;
import com.NutriApp.NutriApp.service.Mail.ManjearMailAsync.MailEvent;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class SolicitudService {

    @Autowired
    private SolicitudRespository solicitudRespository;

    @Autowired
    private AlimentoIngresadoPorUsuarioService alimentoIngresadoPorUsuarioService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private MailService mailService;

    @Autowired
    private FoodDataService foodDataService;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;


    @Transactional
    public SolicitudAltaAlimento insertar (SolicitudAltaAlimento solicitud) throws Exception{
        if (alimentoIngresadoPorUsuarioService.existsByNombre(solicitud.getNombreComida())){
            throw new SolicitudInvalidaException("El alimento ya existe con el nombre = " +solicitud.getNombreComida());
        }

        if (solicitudRespository.existsByNombreComidaIgnoreCase(solicitud.getNombreComida())){
            throw new SolicitudInvalidaException("La solicitud ya existe con el nombre = " +solicitud.getNombreComida());
        }

        if (existeEnLaAPI(solicitud.getNombreComida())){
            throw new SolicitudInvalidaException("El alimento ya existe en la api con el nombre = " + solicitud.getNombreComida());
        }

        // Obtener el nombre de usuario desde el token (ya que está autenticado)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Buscar el usuario en la base de datos
        Usuario usuario = usuarioService.loadUserByUsername(username);

        // Asignar el usuario a la solicitud
        solicitud.setUsuario(usuario);

        // Se le setea la fecha del momento de insertar la solicitud
        solicitud.setFecha(LocalDateTime.now());


        solicitudRespository.save(solicitud);

        eventPublisher.publishEvent(new MailEvent("ekianuruzuna@gmail.com", "Solicitud de Alta de Comida", "Se solicito la alta de esta comida = " +solicitud));
        eventPublisher.publishEvent(new MailEvent("zuriuruzuna6@gmail.com", "Solicitud de Alta de Comida", "Se solicito la alta de esta comida = " +solicitud));
        eventPublisher.publishEvent(new MailEvent("juanignaciovalletorres241104@gmail.com", "Solicitud de Alta de Comida", "Se solicito la alta de esta comida = " +solicitud));

        return solicitud;
    }

    //verifica que no se ecuentre en la api
    private boolean existeEnLaAPI (String nombreComida) throws Exception{
        //obtenemos la lista de alimentos de la api en base a un nombre de alimento
        List<AlimentoBusquedaDTO> alimentosAPI = foodDataService.buscarAlimentosPorNombreSinException(nombreComida);

        //recorremos la lista y nos fijamos si coincide el nombre
        for (AlimentoBusquedaDTO alimento : alimentosAPI){
            if (alimento.getDescripcion().trim().equalsIgnoreCase(nombreComida.trim())){    //el .trim() elimina los espacios al inicio y al final del string
                return true;
            }
        }

        return false;
    }


    // te lista todas las solicitudes ordenadas por fehca de creacion con un limite de 100 para no sobrecarcar
    public List<SolicitudAltaAlimento> listarTodas(){
        if (solicitudRespository.count() == 0){
            return new ArrayList<>();
        }

        return solicitudRespository.findAll().stream()
                .sorted(new Comparator<SolicitudAltaAlimento>() {
                    @Override
                    public int compare(SolicitudAltaAlimento o1, SolicitudAltaAlimento o2) {
                        return o1.getFecha().compareTo(o2.getFecha());
                    }
                })
                .limit(100)
                .toList();
    }

    //filtra todas las solicitudes de una fecha en adelante
    public List<SolicitudAltaAlimento> filtrarSolicitudesPorFecha (LocalDate fechaFiltrar) throws SolicitudInvalidaException{
        if (solicitudRespository.count() == 0){
            throw new SolicitudInvalidaException("No hay solicitudes cargadas");
        }


        List<SolicitudAltaAlimento> solicitudes = solicitudRespository.findAll().stream()
                .sorted(new Comparator<SolicitudAltaAlimento>() {
                    @Override
                    public int compare(SolicitudAltaAlimento o1, SolicitudAltaAlimento o2) {
                        return o1.getFecha().compareTo(o2.getFecha());
                    }
                })
                .filter(x -> !x.getFecha().toLocalDate().isBefore(fechaFiltrar))    //filtra todo lo que no es antes de esa fecha
                .limit(100)
                .toList();

        if (solicitudes.isEmpty()){
            throw new SolicitudInvalidaException("No hay solicitudes cargadas para la fecha = " + fechaFiltrar);
        }

        return solicitudes;
    }

    public List<SolicitudAltaAlimento> filtrarSolicitudesPorUsername (String username) throws SolicitudInvalidaException{
        if (solicitudRespository.count() == 0){
            throw new SolicitudInvalidaException("No hay solicitudes cargadas");
        }

        List<SolicitudAltaAlimento> solicitudes = solicitudRespository.findAllByUsuarioUsername(username);

        if (solicitudes.isEmpty()){
            throw new SolicitudInvalidaException("No hay solicitudes cargadas para el usuario = " + username);
        }

        return solicitudes.stream()
                .sorted(new Comparator<SolicitudAltaAlimento>() {
                    @Override
                    public int compare(SolicitudAltaAlimento o1, SolicitudAltaAlimento o2) {
                        return o1.getFecha().compareTo(o2.getFecha());
                    }
                })
                .limit(100)
                .toList();
    }

    public List<SolicitudAltaAlimento> filtrarPorNombrecomida (String nombreComida) throws SolicitudInvalidaException{
        if (solicitudRespository.count() == 0){
            throw new SolicitudInvalidaException("No hay solicitudes cargadas");
        }

        List<SolicitudAltaAlimento> solicitudes = solicitudRespository.findAllByNombreComidaIgnoreCase(nombreComida);

        if (solicitudes.isEmpty()){
            throw new SolicitudInvalidaException("No hay solicitudes cargadas con el nombre = " + nombreComida);
        }

        return solicitudes.stream()
                .sorted(Comparator.comparing(SolicitudAltaAlimento::getFecha))    //ordena comparando por fecha
                .limit(100)
                .toList();
    }

    public List<SolicitudAltaAlimento> listarMisSolicitudes () throws SolicitudInvalidaException{
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = (Usuario) authentication.getPrincipal();

        List<SolicitudAltaAlimento> list = solicitudRespository.findAllByUsuarioUsername(usuario.getUsername()).stream()
                .sorted(new Comparator<SolicitudAltaAlimento>() {
                    @Override
                    public int compare(SolicitudAltaAlimento o1, SolicitudAltaAlimento o2) {
                        return o1.getFecha().compareTo(o2.getFecha());
                    }
                })
                .limit(100)
                .toList();

        if (list.isEmpty()){
            throw new SolicitudInvalidaException("Usted no tiene nignuna solicitud cargada en el sistema");
        }

        return list;
    }

    @Transactional  //notacion necesaria para todos los metodos que son delete remove y demas
    public String elimiarMiSolicitud (String nombreComidaSolicitudEliminar) throws SolicitudInvalidaException{  //busca en sus solicitudes y si encuentra el mismo nombre de comida (ignora las mayusculas o minusculas) lo elimina, sino tira exception
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = (Usuario) authentication.getPrincipal();      //obtengo el usuario logeado

        if (solicitudRespository.existsByUsuarioUsernameAndNombreComidaIgnoreCase(usuario.getUsername(), nombreComidaSolicitudEliminar)){ //si el usuario logeado tiene esa solicitud

            solicitudRespository.deleteByUsuarioUsernameAndNombreComidaIgnoreCase(usuario.getUsername(), nombreComidaSolicitudEliminar);  //se elimina la solicitud
            return "Solicitud eliminada con exito";
        }



        throw new SolicitudInvalidaException("Usted no tiene ninguna solicitud cargada con el nombre de comida = " + nombreComidaSolicitudEliminar);    //tira exception porque ese usuaior no tiene esa solicitud
    }


    //se fija en la solicitud que se quiere modificar y solo le setea los campos nuevos que vienen como entrada (no hace falta mandar todos los campos en la entrada)
    @Transactional
    public SolicitudAltaAlimento modificarMiSolicitud (String nombreComidaSolicitudModificar, SolicitudAltaAlimento solicitudNueva) throws SolicitudInvalidaException{


        //si el nombre del objeto nuevo no cambio con respecto a no modificado
        if (!nombreComidaSolicitudModificar.equals(solicitudNueva.getNombreComida())){
            //busca en las solicitudes para que no se pisen los nombres con otras solicitudes
            if (solicitudRespository.existsByNombreComidaIgnoreCase(solicitudNueva.getNombreComida())){
                throw new SolicitudInvalidaException("La solicitud ya existe con el nombre = " +solicitudNueva.getNombreComida());
            }

            //busca en los nombres de los alimentos ingresados por el usuario
            if (alimentoIngresadoPorUsuarioService.existsByNombre(solicitudNueva.getNombreComida())){
                throw new SolicitudInvalidaException("El alimento ya existe con el nombre = " +solicitudNueva.getNombreComida());
            }
        }


        //obtenemos el usuario logeado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = (Usuario) authentication.getPrincipal();

        //obtenemos la solicitud a la que se quiere actualizar los campos
        Optional<SolicitudAltaAlimento> solicitudVieja = solicitudRespository.findByUsuarioUsernameAndNombreComidaIgnoreCase(usuario.getUsername(), nombreComidaSolicitudModificar);

        //comprobamos que exista la solicitud a la que se quiere modificar
        if (solicitudVieja.isEmpty()){
            throw new SolicitudInvalidaException("Usted no tiene ninguna solicitud cargada con el nombre de comida = " + nombreComidaSolicitudModificar);
        }

        //seteos de los campos nuevos a la vieja solicitud
        solicitudVieja.get().setearDatosDesdeNuevaSolicitud(solicitudNueva);  // se lo seteamos porque si viene nulo, cuando hacemos el save no nos va a dejar


        //guardamos el objeto modificado
        solicitudRespository.save(solicitudVieja.get());  //el save tambien reemplaza todos los valores de un objeto si ya esta creado en la bdd
        return solicitudVieja.get();
    }


    //ADMINS

    @Transactional
    public String modificar_Y_AceptarSolicitud (String nombreComidaSolicitudMoficiar, SolicitudAltaAlimento solicitudNueva){
        System.out.println("Antes de la comprobacion");



        System.out.println("Nombre de la solicitud vieja" + nombreComidaSolicitudMoficiar);
        System.out.println("Nombre de la solicitud nueva" + solicitudNueva.getNombreComida());

        //si el nombre del objeto nuevo no cambio con respecto a no modificado
        if (!nombreComidaSolicitudMoficiar.equals(solicitudNueva.getNombreComida())){
            //busca en las solicitudes para que no se pisen los nombres con otras solicitudes
            if (solicitudRespository.existsByNombreComidaIgnoreCase(solicitudNueva.getNombreComida())){
                throw new SolicitudInvalidaException("La solicitud ya existe con el nombre = " +solicitudNueva.getNombreComida());
            }

            //busca en los nombres de los alimentos ingresados por el usuario
            if (alimentoIngresadoPorUsuarioService.existsByNombre(solicitudNueva.getNombreComida())){
                throw new SolicitudInvalidaException("El alimento ya existe con el nombre = " +solicitudNueva.getNombreComida());
            }
        }
        System.out.println("Despues de la comprobacion");


        //obtenemos la solicitud a la que se quiere modificar
        Optional<SolicitudAltaAlimento> solicitudAltaAlimentoOptional = solicitudRespository.findByNombreComidaIgnoreCase(nombreComidaSolicitudMoficiar);

        //comprobamos si existe
        if (solicitudAltaAlimentoOptional.isEmpty()){
            throw new SolicitudInvalidaException("La solicitud no existe con el nombre de comida: " + nombreComidaSolicitudMoficiar);
        }

        //seteamos los campos con la nueva solicitud
        solicitudAltaAlimentoOptional.get().setearDatosDesdeNuevaSolicitud(solicitudNueva);

        //se inserta el alimento en la bdd
        alimentoIngresadoPorUsuarioService.insertarBasandoseEnSolicitud(solicitudAltaAlimentoOptional.get());

        //se elimina de la tabla solicitudes
        solicitudRespository.deleteById(solicitudAltaAlimentoOptional.get().getId());

        //se notifica al usuario que se acepto la solicitud de forma asyncronica con disparador de eventos
        // y listeners de esos eventos asi no afecta al @Transactional que tiene este metodo
        eventPublisher.publishEvent(new MailEvent(
                obtenerMail(solicitudAltaAlimentoOptional.get().getUsername()),
                "Aceptacion de solicitud",
                "Su solicitud de alta de comida con el nombre '" + solicitudAltaAlimentoOptional.get().getNombreComida() + "' fue aceptada")

        );

        return "Solicitud aceptada con exito y alimento ingresado correctamente";
    }

    @Transactional
    public String aceptarSolicitud (long idSolicitud) throws SolicitudInvalidaException{
        //bucamos la solicitud
        Optional<SolicitudAltaAlimento> solicitud = solicitudRespository.findById(idSolicitud);

        //se comprueba que exista
        if (solicitud.isEmpty()){
            throw new SolicitudInvalidaException("No se ecnontro la solicitud con el id = " + idSolicitud);
        }

        //se inserta el alimento en la bdd
        alimentoIngresadoPorUsuarioService.insertarBasandoseEnSolicitud(solicitud.get());

        //se borra de la tabla la solicitud
        solicitudRespository.deleteById(solicitud.get().getId());

        //se notifica al usuario que se acepto la solicitud de forma asyncronica con disparador de eventos
        // y listeners de esos eventos asi no afecta al @Transactional que tiene este metodo
        eventPublisher.publishEvent(new MailEvent(
                obtenerMail(solicitud.get().getUsername()),
                "Aceptacion de solicitud",
                "Su solicitud de alta de comida con el nombre '" + solicitud.get().getNombreComida() + "' fue aceptada")

        );

        return "Solicitud aceptada con exito y alimento ingresado correctamente";
    }

    @Transactional
    public String rechazarSolicitud (long idSolicitud) throws SolicitudInvalidaException{
        //buscamos la solicitud
        Optional<SolicitudAltaAlimento> solicitud = solicitudRespository.findById(idSolicitud);

        //se comprueba que exista
        if (solicitud.isEmpty()){
            throw new SolicitudInvalidaException("No se encontro la solicitud con el id = " + idSolicitud);
        }

        //eliminamos la solicitud
        solicitudRespository.deleteById(solicitud.get().getId());

        //se notifica al usuario que se rechazo la solicitud
        eventPublisher.publishEvent(new MailEvent(
                obtenerMail(solicitud.get().getUsername()),
                "Rechazo de solicitud",
                "Su solicitud de alta de comida con el nombre '" + solicitud.get().getNombreComida() + "' fue rechazada")
                );

        return "Solicitud rechazada con exito";
    }


    //obtiene un mail en base a un username
    private String obtenerMail (String username){
        Persona persona = personaService.obtenerPorUsername(username);

        return persona.getEmail();
    }
}
