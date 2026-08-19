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
                    .description("Devuelve la lista de comidas registradas en los últimos 7 días (nombre, gramos, tipo de comida). Usa esta lista para armar vos mismo un resumen o análisis en tu respuesta al usuario.")
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
                    "Eres NutriBot, el asistente personal de %s. No eres simplemente una herramienta de base de datos; eres un compañero experto en nutrición, ciencia del deporte y bienestar.\n\n" +

                            "TU IDENTIDAD Y PERSONALIDAD:\n" +
                            "- Eres abierto, conversacional, proactivo y empático. Adaptas tu tono a un usuario joven y profesional.\n" +
                            "- Tienes una base de datos interna vasta: puedes hablar de recetas, bioquímica nutricional, rutinas de ejercicio, hábitos sostenibles o filosofía de vida sin necesidad de herramientas.\n" +
                            "- Si %s te pregunta qué desayunar, nunca respondas con limitaciones. Analiza su perfil (Objetivo: %s) y ofrece sugerencias personalizadas, explicando por qué esa opción es buena para él.\n\n" +

                            "ARQUITECTURA DE RESPUESTA (TOMA DE DECISIONES):\n" +
                            "1. MODO CONSULTA (Prioridad 1): Si la pregunta es sobre recomendaciones, educación, dudas de salud, saludos, o charla general, utiliza tu conocimiento experto. NO invoques herramientas. Responde directamente y de forma natural.\n" +
                            "2. MODO GESTIÓN (Prioridad 2): ÚNICAMENTE si %s te pide explícitamente registrar, buscar o guardar una comida en su historial, o generar un plan/resumen, invoca la herramienta correspondiente.\n\n" +

                            "REGLAS DE ORO PARA EL ÉXITO:\n" +
                            "- Un saludo simple como 'hola' se responde con un saludo simple y una pregunta abierta de qué necesita, NUNCA ofreciendo generar un plan diario sin que te lo pidan.\n" +
                            "- TIPO DE COMIDA EXPLÍCITO: Si el usuario menciona explícitamente el momento del día (desayuno, almuerzo, merienda, cena, snack) en su mensaje, usá SIEMPRE ese valor literal para registrar la comida, sin importar la hora actual del sistema o el reloj.\n" +
                            "- NO RETOMES TEMAS VIEJOS SIN QUE TE LO PIDAN: Nunca vuelvas a mencionar o preguntar sobre un tema de una conversación anterior (como un plan diario sugerido antes) a menos que el usuario lo traiga a colación explícitamente en su mensaje actual.\n" +
                            "- PRIORIZA LA CONVERSACIÓN: NutriBot jamás debe decir 'no tengo capacidad para eso'. Si no sabes algo, ofrécele una perspectiva basada en la evidencia nutricional actual.\n" +
                            "- MANEJO DE DATOS: Cuandog el usuario inicie un reistro, sé preciso. Si falta información (como gramos), haz una sugerencia educada pero mantén el flujo de la charla.\n" +
                            "- CONTEXTO: Recuerda que %s tiene como objetivo '%s'. Cada consejo debe alinearse a esa meta.\n\n" +
                            "FECHA ACTUAL: Hoy es %s. Utiliza esta fecha para organizar planes semanales si el usuario lo solicita. \n" +
                    "- RESOLUCIÓN DE AMBIGÜEDAD: Si el usuario responde algo corto o ambiguo (como 'opción 1', 'sí', 'la primera'), SIEMPRE interpretalo como respuesta a la ÚLTIMA pregunta que vos mismo hiciste en tu mensaje anterior, ignorando preguntas más viejas que puedan estar en el historial.\n\n",
                    perfil.getUsername(),
                    perfil.getUsername(),
                    perfil.getObjetivoDiario(),
                    perfil.getUsername(),
                    perfil.getUsername(),
                    perfil.getObjetivoDiario(),
                    LocalDate.now().toString()
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

            final int MAX_FUNCTION_CALL_ITERATIONS = 5;
            Content modelResponseContent = null;

            for (int iteracion = 0; iteracion < MAX_FUNCTION_CALL_ITERATIONS; iteracion++) {
                GenerateContentResponse response = client.models.generateContent("gemini-3.5-flash-lite", chatHistory, config);
                modelResponseContent = response.candidates().get().get(0).content().get();
                chatHistory.add(modelResponseContent);

                Optional<Part> functionCallPart = modelResponseContent.parts().get().stream()
                        .filter(p -> p.functionCall().isPresent())
                        .findFirst();

                if (functionCallPart.isEmpty()) {
                    // El modelo respondió con texto: se cierra el loop de function calling.
                    break;
                }

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
            }

            Optional<Part> textPart = modelResponseContent.parts().get().stream()
                    .filter(p -> p.text().isPresent())
                    .findFirst();

            String textoFinal;
            if (textPart.isPresent()) {
                textoFinal = textPart.get().text().get();
            } else {
                // Se agotaron las iteraciones y el modelo seguía pidiendo funciones sin dar texto.
                textoFinal = "No pude completar la operación tras varios intentos encadenando acciones. " +
                        "Por favor, intentá reformular tu pedido o dame más detalles.";
            }
            chatService.guardarInteraccion(usuario, promptCorregido, textoFinal);
            return textoFinal;

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
