package com.NutriApp.NutriApp.config;


import com.NutriApp.NutriApp.modelo.dto.ChatRequest;
import com.NutriApp.NutriApp.service.GeminiService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "http://localhost:4200") // CORS habilitado para tu Angular
public class AIController {

    private final GeminiService geminiService;

    public AIController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/ask")
    public Map<String, String> ask(@RequestBody ChatRequest request) {
        // Llamamos al servicio de Gemini
        String respuesta = geminiService.ask(request.getMessage());

        // Retornamos un mapa que Spring Boot convertirá a JSON automáticamente
        Map<String, String> response = new HashMap<>();
        response.put("reply", respuesta);

        return response;
    }
}
