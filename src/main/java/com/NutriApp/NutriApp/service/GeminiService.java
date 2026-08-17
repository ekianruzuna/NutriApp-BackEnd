package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.modelo.PerfilNutricional;
import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.dto.AlimentoBusquedaDTO;
import com.NutriApp.NutriApp.modelo.enums.TipoComida;
import com.google.genai.Client;
import com.google.genai.types.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final List<Content> chatHistory = new ArrayList<>();

    @Autowired
    private ComidaIngeridaService comidaIngeridaService;
    @Autowired
    private AlimentoGlobalService alimentoGlobalService;
    @Autowired
    private PerfilNutricionalService perfilNutricionalService;
    @Autowired
    private ChatService chatService;

    public String ask(String prompt) {
        try {
            Client client = Client.builder().apiKey(apiKey).build();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = (Usuario) auth.getPrincipal();

            List<Content> historial = chatService.cargarHistorialComoContent(usuario.getUsername());
            chatHistory.clear();
            chatHistory.addAll(historial);

            PerfilNutricional perfil = perfilNutricionalService.obtenerPerfilNutricional();
            String fechaHoy = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String promptCorregido = prompt.replace("hoy", "el día " + fechaHoy);

            // Definición de herramientas
            FunctionDeclaration buscarAlimentoFunc = FunctionDeclaration.builder()
                    .name("buscarAlimento")
                    .description("Busca alimentos en la API por nombre para obtener opciones exactas.")
                    .parameters(Schema.builder()
                            .type("OBJECT")
                            .properties(Map.of("nombre", Schema.builder().type("STRING").build()))
                            .build())
                    .build();

            FunctionDeclaration agregarComidaFunc = FunctionDeclaration.builder()
                    .name("agregarComidaIngerida")
                    .description("Registra un alimento ingerido.")
                    .parameters(Schema.builder()
                            .type("OBJECT")
                            .properties(Map.of(
                                    "nombreComida", Schema.builder().type("STRING").build(),
                                    "id", Schema.builder().type("NUMBER").build(),
                                    "gramos", Schema.builder().type("NUMBER").build(),
                                    "tipoComida", Schema.builder().type("STRING").build(),
                                    "fecha", Schema.builder()
                                            .type("STRING")
                                            .description("La fecha del consumo en formato YYYY-MM-DD. " +
                                                    "Si el usuario dice 'ayer', 'mañana' o una fecha específica, " +
                                                    "calculá la fecha correspondiente. Si no se menciona, usá: " + fechaHoy)
                                            .build()
                            ))
                            .build())
                    .build();

            FunctionDeclaration resumenSemanalFunc = FunctionDeclaration.builder()
                    .name("generarResumenSemanal")
                    .description("Genera un análisis estadístico de los alimentos ingeridos en los últimos 7 días.")
                    .build();

            FunctionDeclaration generarPlanDiarioFunc = FunctionDeclaration.builder()
                    .name("generarPlanDiario")
                    .description("Genera un plan de alimentación para hoy según el objetivo e historial del usuario.")
                    .parameters(Schema.builder()
                            .type("OBJECT")
                            .properties(Map.of("diaEntrenamiento", Schema.builder()
                                    .type("BOOLEAN")
                                    .description("Indica si el usuario entrenará hoy.")
                                    .build()))
                            .build())
                    .build();

            Tool functionsTool = Tool.builder()
                    .functionDeclarations(List.of(
                            agregarComidaFunc, buscarAlimentoFunc, resumenSemanalFunc, generarPlanDiarioFunc
                    ))
                    .build();

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .tools(List.of(functionsTool))
                    .build();

            String systemInstructions = String.format(
                    "Eres NutriBot, el asistente personal de %s...\n\nFECHA ACTUAL: Hoy es %s.",
                    perfil.getUsername(), LocalDate.now().toString()
            );
            List<Content> chatHistoryConSistema = new ArrayList<>();
            chatHistoryConSistema.add(Content.builder().role("user")
                    .parts(List.of(Part.builder().text(systemInstructions).build()))
                    .build());
            chatHistoryConSistema.addAll(chatHistory);
            chatHistory.clear();
            chatHistory.addAll(chatHistoryConSistema);

            chatHistory.add(Content.builder().role("user")
                    .parts(List.of(Part.builder().text(promptCorregido).build()))
                    .build());

            GenerateContentResponse response = client.models.generateContent("gemini-3.6-flash", chatHistory, config);
            Content modelResponseContent = response.candidates().get().get(0).content().get();
            chatHistory.add(modelResponseContent);

            Optional<Part> functionCallPart = modelResponseContent.parts().get().stream()
                    .filter(p -> p.functionCall().isPresent())
                    .findFirst();

            if (functionCallPart.isPresent()) {
                FunctionCall call = functionCallPart.get().functionCall().get();
                String resultadoFuncion = "";
                Map<String, Object> args = call.args().orElse(Map.of());

                switch (call.name().orElse("")) {
                    case "agregarComidaIngerida" -> resultadoFuncion = ejecutarRegistro(args);
                    case "buscarAlimento" -> resultadoFuncion = ejecutarBusqueda(args);
                    case "generarResumenSemanal" -> resultadoFuncion = chatService.obtenerResumenSemanal(usuario.getUsername());
                    case "generarPlanDiario" -> {
                        boolean entrena = args.containsKey("diaEntrenamiento") && (boolean) args.get("diaEntrenamiento");
                        resultadoFuncion = chatService.generarPlanDiario(usuario.getUsername(), entrena);
                    }
                }

                chatHistory.add(Content.builder().role("user")
                        .parts(List.of(Part.builder().text("Resultado de '" + call.name().orElse("") + "': " + resultadoFuncion).build()))
                        .build());

                GenerateContentResponse responseFinal = client.models.generateContent("gemini-3.6-flash", chatHistory, config);
                Content finalContent = responseFinal.candidates().get().get(0).content().get();
                chatHistory.add(finalContent);

                String textoFinal;
                Optional<Part> textPart = finalContent.parts().get().stream()
                        .filter(p -> p.text().isPresent())
                        .findFirst();

                if (textPart.isPresent()) {
                    textoFinal = textPart.get().text().get();
                } else {
                    textoFinal = "Necesito un poco más de información para completar esto. ¿Podrías darme más detalles?";
                }
                chatService.guardarInteraccion(usuario, promptCorregido, textoFinal);
                return textoFinal;
            }

            String textoDirecto = modelResponseContent.parts().get().get(0).text().orElse("");
            chatService.guardarInteraccion(usuario, promptCorregido, textoDirecto);
            return textoDirecto;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error en NutriBot: " + e.getMessage();
        }
    }

    private String ejecutarBusqueda(Map<String, Object> args) {
        try {
            String nombre = (String) args.get("nombre");
            List<AlimentoBusquedaDTO> resultados = alimentoGlobalService.filtrarAlimentosCombinadosPorNombreComida(nombre);

            if (resultados == null || resultados.isEmpty()) {
                return "No encontré alimentos que coincidan con '" + nombre + "'.";
            }

            String opciones = resultados.stream()
                    .limit(5)
                    .map(a -> a.getDescripcion() + " (ID: " + a.getFdcId() + ")")
                    .collect(Collectors.joining(", "));

            return "He encontrado las siguientes opciones para '" + nombre + "': " + opciones + ". Pedile al usuario que elija una.";
        } catch (Exception e) {
            return "Hubo un error al buscar en la base de datos: " + e.getMessage();
        }
    }

    private String ejecutarRegistro(Map<String, Object> args) {
        try {
            String nombre = args.containsKey("nombreComida") ? (String) args.get("nombreComida") : "Desconocido";
            double gramos = args.containsKey("gramos") ? ((Number) args.get("gramos")).doubleValue() : 0.0;
            String tipoStr = args.containsKey("tipoComida") ? (String) args.get("tipoComida") : "SNACK";
            String fechaStr = args.containsKey("fecha") && args.get("fecha") != null
                    ? (String) args.get("fecha") : LocalDate.now().toString();
            long idComida = args.containsKey("id") ? ((Number) args.get("id")).longValue() : 0L;

            TipoComida tipo = TipoComida.valueOf(tipoStr.toUpperCase());
            LocalDate fecha = LocalDate.parse(fechaStr);

            comidaIngeridaService.agregarComidaIngerida(idComida, nombre, gramos, tipo, fecha);
            return "¡Éxito! Registré " + gramos + "g de " + nombre + ".";
        } catch (Exception e) {
            return "Error al registrar: " + e.getMessage();
        }
    }
}
