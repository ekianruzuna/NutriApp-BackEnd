package com.NutriApp.NutriApp.service;
import com.NutriApp.NutriApp.modelo.ChatMessage;
import com.NutriApp.NutriApp.modelo.Comida;
import com.NutriApp.NutriApp.modelo.PerfilNutricional;
import com.NutriApp.NutriApp.modelo.Usuario;
import com.NutriApp.NutriApp.modelo.dto.AlimentoBusquedaDTO;
import com.NutriApp.NutriApp.modelo.enums.TipoComida;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.*;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.google.cloud.vertexai.api.GoogleSearchRetrieval;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.google.protobuf.Struct;
import java.util.stream.Collectors;
@Service
public class GeminiService {

    // Esta lista actúa como la "memoria" de la sesión
    private final List<Content> chatHistory = new ArrayList<>();

    @Autowired
    private ComidaIngeridaService comidaIngeridaService; // Tu servicio existente
    @Autowired
    private AlimentoGlobalService alimentoGlobalService;
    @Autowired
    private PerfilNutricionalService perfilNutricionalService;
    @Autowired
    private ChatService chatService;

    public String ask(String prompt) {
        try (VertexAI vertexAI = new VertexAI("nutriapp-61192", "us-central1")) {


            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = (Usuario) auth.getPrincipal();

            // 1. Cargar memoria desde el service
            List<Content> historial = chatService.cargarHistorialComoContent(usuario.getUsername());
            chatHistory.clear();
            chatHistory.addAll(historial);

            // 1. Obtener perfil y contexto de fecha
            PerfilNutricional perfil = perfilNutricionalService.obtenerPerfilNutricional();
            String fechaHoy = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String promptCorregido = prompt.replace("hoy", "el día " + fechaHoy);

            // 2. Definición de herramientas
            FunctionDeclaration buscarAlimentoFunc = FunctionDeclaration.newBuilder()
                    .setName("buscarAlimento")
                    .setDescription("Busca alimentos en la API por nombre para obtener opciones exactas.")
                    .setParameters(Schema.newBuilder()
                            .setType(Type.OBJECT)
                            .putProperties("nombre", Schema.newBuilder().setType(Type.STRING).build())
                            .build())
                    .build();

            FunctionDeclaration agregarComidaFunc = FunctionDeclaration.newBuilder()
                    .setName("agregarComidaIngerida")
                    .setDescription("Registra un alimento ingerido.")
                    .setParameters(Schema.newBuilder()
                            .setType(Type.OBJECT)
                            .putProperties("nombreComida", Schema.newBuilder().setType(Type.STRING).build())
                            .putProperties("id", Schema.newBuilder().setType(Type.NUMBER).build())
                            .putProperties("gramos", Schema.newBuilder().setType(Type.NUMBER).build())
                            .putProperties("tipoComida", Schema.newBuilder().setType(Type.STRING).build())
                            // IMPORTANTE: Explicar que el formato debe ser YYYY-MM-DD
                            .putProperties("fecha", Schema.newBuilder()
                                    .setType(Type.STRING)
                                    .setDescription("La fecha del consumo en formato YYYY-MM-DD. " +
                                            "Es vital que si el usuario dice 'ayer', 'mañana' o una fecha específica, " +
                                            "calcules la fecha correspondiente y la incluyas aquí. " +
                                            "Si no se menciona ninguna fecha, omite este campo o usa la fecha actual: " + fechaHoy)
                                    .build()))
                    .build();

            FunctionDeclaration resumenSemanalFunc = FunctionDeclaration.newBuilder()
                    .setName("generarResumenSemanal")
                    .setDescription("Genera un análisis estadístico de los alimentos ingeridos en los últimos 7 días. Úsalo cuando el usuario pida 'resumen semanal'.")
                    .build();

            FunctionDeclaration generarPlanDiarioFunc = FunctionDeclaration.newBuilder()
                    .setName("generarPlanDiario")
                    .setDescription("Genera un plan de alimentación para el día de hoy basado en el objetivo del usuario y su historial de comidas.")
                    .setParameters(Schema.newBuilder()
                            .setType(Type.OBJECT)
                            .putProperties("diaEntrenamiento", Schema.newBuilder()
                                    .setType(Type.BOOLEAN)
                                    .setDescription("Indica si el usuario entrenará hoy para ajustar las calorías.")
                                    .build())
                            .build())
                    .build();

            // 1. Herramientas propias
            Tool functionsTool = Tool.newBuilder()
                    .addFunctionDeclarations(agregarComidaFunc)
                    .addFunctionDeclarations(buscarAlimentoFunc)
                    .addFunctionDeclarations(resumenSemanalFunc)
                    .addFunctionDeclarations(generarPlanDiarioFunc)
                    .build();




// 3. Inicialización del modelo
            // Configurar el modelo con las herramientas Y el buscador de Google activado
            GenerativeModel model = new GenerativeModel("gemini-2.5-flash", vertexAI)
                    .withTools(Arrays.asList(functionsTool));

            // 3. Inyección de Instrucciones de Sistema (Contexto Dinámico)
            if (chatHistory.isEmpty()) {
                        String systemInstructions = String.format(
                                "Eres NutriBot, el asistente personal de %s. No eres simplemente una herramienta de base de datos; eres un compañero experto en nutrición, ciencia del deporte y bienestar.\n\n" +

                                        "TU IDENTIDAD Y PERSONALIDAD:\n" +
                                        "- Eres abierto, conversacional, proactivo y empático. Adaptas tu tono a un usuario joven y profesional.\n" +
                                        "- Tienes una base de datos interna vasta: puedes hablar de recetas, bioquímica nutricional, rutinas de ejercicio, hábitos sostenibles o filosofía de vida sin necesidad de herramientas.\n" +
                                        "- Si %s te pregunta qué desayunar, nunca respondas con limitaciones. Analiza su perfil (Objetivo: %s) y ofrece sugerencias personalizadas, explicando por qué esa opción es buena para él.\n\n" +

                                        "ARQUITECTURA DE RESPUESTA (TOMA DE DECISIONES):\n" +
                                        "1. MODO CONSULTA (Prioridad 1): Si la pregunta es sobre recomendaciones, educación, dudas de salud o charla general, utiliza tu conocimiento experto. NO invoques herramientas. Responde directamente.\n" +
                                        "2. MODO GESTIÓN (Prioridad 2): ÚNICAMENTE si %s te pide explícitamente registrar, buscar o guardar una comida en su historial, invoca 'buscarAlimento' o 'agregarComidaIngerida'.\n\n" +

                                        "REGLAS DE ORO PARA EL ÉXITO:\n" +
                                        "- PRIORIZA LA CONVERSACIÓN: NutriBot jamás debe decir 'no tengo capacidad para eso'. Si no sabes algo, ofrécele una perspectiva basada en la evidencia nutricional actual.\n" +
                                        "- MANEJO DE DATOS: Cuando el usuario inicie un registro, sé preciso. Si falta información (como gramos), haz una sugerencia educada pero mantén el flujo de la charla.\n" +
                                        "- CONTEXTO: Recuerda que %s tiene como objetivo '%s'. Cada consejo debe alinearse a esa meta.\n\n" +
                                        "FECHA ACTUAL: Hoy es %s. Utiliza esta fecha para organizar planes semanales si el usuario lo solicita.",
                                perfil.getUsername(),
                                perfil.getEdad(),
                                perfil.getPeso(),
                                perfil.getNivelActividadFisica(),
                                perfil.getObjetivoCaloricoTipo(),
                                perfil.getObjetivoDiario(),
                                LocalDate.now().toString()
                );
                chatHistory.add(Content.newBuilder().setRole("user").addParts(Part.newBuilder().setText(systemInstructions).build()).build());
            }

            // 4. Agregar mensaje del usuario
            chatHistory.add(Content.newBuilder().setRole("user").addParts(Part.newBuilder().setText(promptCorregido).build()).build());

            // 5. Generar respuesta
            GenerateContentResponse response = model.generateContent(chatHistory);
            Content modelResponseContent = response.getCandidates(0).getContent();
            chatHistory.add(modelResponseContent);

            // 6. Router (Lógica de llamadas a funciones)
            if (modelResponseContent.getPartsCount() > 0 && modelResponseContent.getParts(0).hasFunctionCall()) {
                FunctionCall call = modelResponseContent.getParts(0).getFunctionCall();
                String resultadoFuncion = "";

                if (call.getName().equals("agregarComidaIngerida")) {
                    resultadoFuncion = ejecutarRegistro(call.getArgs());
                } else if (call.getName().equals("buscarAlimento")) {
                    resultadoFuncion = ejecutarBusqueda(call.getArgs());
                } else if (call.getName().equals("generarResumenSemanal")) {
                    resultadoFuncion = chatService.obtenerResumenSemanal(usuario.getUsername());
                }else if (call.getName().equals("generarPlanDiario")) {
                    // 1. Obtenemos el campo de forma segura
                    com.google.protobuf.Value campoEntrena = call.getArgs().getFieldsOrDefault(
                            "diaEntrenamiento",
                            com.google.protobuf.Value.newBuilder().setBoolValue(false).build()
                    );

                    // 2. Extraemos el booleano
                    boolean entrena = campoEntrena.getBoolValue();

                    // 3. Llamamos al servicio
                    resultadoFuncion = chatService.generarPlanDiario(usuario.getUsername(), entrena);
                }


                // Retroalimentación al modelo
                chatHistory.add(Content.newBuilder()
                        .setRole("user")
                        .addParts(Part.newBuilder().setText("Resultado de la ejecución de '" + call.getName() + "': " + resultadoFuncion).build())
                        .build());

                GenerateContentResponse responseFinal = model.generateContent(chatHistory);
                Content finalContent = responseFinal.getCandidates(0).getContent();
                chatHistory.add(finalContent);

                // Definimos el texto final aquí
                String textoFinal = finalContent.getParts(0).getText();

                // Guardar la interacción en la base de datos para futuras consultas
                chatService.guardarInteraccion(usuario, promptCorregido, textoFinal);

                return textoFinal;
            }

            // Y no olvides guardar también si el bot respondió directamente (sin llamar a funciones)
            String textoDirecto = modelResponseContent.getParts(0).getText();
            chatService.guardarInteraccion(usuario, promptCorregido, textoDirecto);

            return textoDirecto;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error en NutriBot: " + e.getMessage();
        }
    }




    private String ejecutarBusqueda(com.google.protobuf.Struct args) {
        try {
            // 1. Extraemos el nombre del alimento que la IA decidió buscar
            String nombre = args.getFieldsMap().get("nombre").getStringValue();

            // 2. Llamamos a tu servicio existente
            List<AlimentoBusquedaDTO> resultados = alimentoGlobalService.filtrarAlimentosCombinadosPorNombreComida(nombre);

            // 3. Verificamos si hay resultados
            if (resultados == null || resultados.isEmpty()) {
                return "No encontré alimentos que coincidan con '" + nombre + "'. Por favor, intenta con otro nombre.";
            }

            // 4. Transformamos la lista a un formato que la IA pueda leer fácilmente
            // Usamos el campo descripción (o el que corresponda en tu DTO)
            // En tu método ejecutarBusqueda
            String opciones = resultados.stream()
                    .limit(5)
                    .map(alimento -> alimento.getDescripcion() + " (ID: " + alimento.getFdcId() + ")") // <--- ¡MUY IMPORTANTE!
                    .collect(Collectors.joining(", "));

            return "He encontrado las siguientes opciones para '" + nombre + "': " + opciones + ". Por favor, dile al usuario que elija una o especifique mejor.";

        } catch (Exception e) {
            System.out.println("Error en la búsqueda: " + e.getMessage());
            return "Hubo un error al buscar en la base de datos: " + e.getMessage();
        }
    }


    private String ejecutarRegistro(com.google.protobuf.Struct args) {
        try {
            java.util.Map<String, com.google.protobuf.Value> fields = args.getFieldsMap();

            // LOG CRÍTICO
            System.out.println("DEBUG IA - Campos recibidos: " + fields.toString());

            // Helper para extraer datos de forma segura
            String nombre = fields.containsKey("nombreComida") ? fields.get("nombreComida").getStringValue() : "Desconocido";

            // Verifica si la clave existe antes de acceder
            double gramos = fields.containsKey("gramos") ? fields.get("gramos").getNumberValue() : 0.0;

            String tipoStr = fields.containsKey("tipoComida") ? fields.get("tipoComida").getStringValue() : "SNACK";
            String fechaStr;
            if (fields.containsKey("fecha") && !fields.get("fecha").getStringValue().isEmpty()) {
                fechaStr = fields.get("fecha").getStringValue();
            } else {
                fechaStr = LocalDate.now().toString();
            }

            // ID: Asegúrate de buscar la clave correcta que definiste en el Schema (idComida o id_comida)
            long idComida = fields.containsKey("id") ? (long) fields.get("id").getNumberValue() : 0L;

            TipoComida tipo = TipoComida.valueOf(tipoStr.toUpperCase());
            LocalDate fecha = LocalDate.parse(fechaStr);

            // USAR EL ID REAL AQUÍ:
            comidaIngeridaService.agregarComidaIngerida(idComida, nombre, gramos, tipo, fecha);

            return "¡Éxito! Registré " + gramos + "g de " + nombre + ".";
        } catch (Exception e) {
            return "Error al registrar: " + e.getMessage();
        }
    }
}
