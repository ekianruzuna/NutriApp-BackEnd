package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.modelo.ChatMessage;
import com.NutriApp.NutriApp.modelo.Comida;
import com.NutriApp.NutriApp.modelo.ComidaIngerida;
import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.repository.ChatRepository;
import com.NutriApp.NutriApp.repository.ComidaIngeridaRepository;
import com.NutriApp.NutriApp.repository.PerfilNutricionalRepository;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.Part;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;     // Importante
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ComidaIngeridaRepository comidaIngeridaRepository;
    @Autowired
    private PerfilNutricionalService perfilNutricionalService;

    // Recupera y convierte a formato IA (limpiando datos viejos si es necesario)
    public List<Content> cargarHistorialComoContent(String usuario) {
        List<ChatMessage> mensajes = chatRepository.findTop20ByUsuarioUsernameOrderByTimestampAsc(usuario);

        return mensajes.stream()
                .map(msg -> Content.newBuilder()
                        .setRole(msg.getRol().toLowerCase())
                        .addParts(Part.newBuilder().setText(msg.getContenido()).build())
                        .build())
                .collect(Collectors.toList());
    }

    // Guarda los mensajes nuevos
    @Transactional
    public void guardarInteraccion(Usuario usuario, String prompt, String respuesta) {
        chatRepository.save(new ChatMessage(null, usuario, prompt, "USER", LocalDateTime.now()));
        chatRepository.save(new ChatMessage(null, usuario, respuesta, "MODEL", LocalDateTime.now()));
    }

    // En tu ChatService o un servicio nuevo
    public String obtenerResumenSemanal(String username) {
        LocalDate fin = LocalDate.now();
        LocalDate inicio = fin.minusDays(7);

        // Aquí consultas tu DB
        List<ComidaIngerida> comidasSemana = comidaIngeridaRepository.findByUsernameAndFechaBetween(username, inicio, fin);

        // Transformas esto a un JSON o String simple para el modelo
        return comidasSemana.stream()
                .map(c -> c.getNombreComida() + ": " + c.getCantidad() + "g - " + c.getTipoComida())
                .collect(Collectors.joining("\n"));
    }


    public String generarPlanDiario(String username, boolean entrena) {
        // 1. Obtenemos los nombres (ya no objetos completos)
        List<String> favoritos = comidaIngeridaRepository.findTopFrecuenciaNombres(username)
                .stream().limit(5).toList();

        // 2. Construir el contexto
        StringBuilder sb = new StringBuilder();
        sb.append("Objetivo del usuario: ").append(perfilNutricionalService.obtenerPerfilNutricional().getObjetivoDiario()).append("\n");
        sb.append("Contexto: ").append(entrena ? "El usuario entrena hoy. Ajustar macros." : "Día de descanso.").append("\n");
        sb.append("Comidas sugeridas (basado en favoritos): ");

        // Aquí solo concatenamos nombres
        sb.append(String.join(", ", favoritos));

        return sb.toString();
    }
}
