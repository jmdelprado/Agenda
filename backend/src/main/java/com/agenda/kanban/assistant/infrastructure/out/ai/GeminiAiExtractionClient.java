package com.agenda.kanban.assistant.infrastructure.out.ai;

import com.agenda.kanban.assistant.application.port.out.AiExtractionClientPort;
import com.agenda.kanban.assistant.domain.exception.AiExtractionException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Implementa {@link AiExtractionClientPort} con la API gratuita de Google Gemini (Google AI
 * Studio, modelo {@code gemini-3.6-flash}) en vez de la API de pago de Claude. Se le pide a
 * Gemini JSON puro vía {@code responseMimeType: application/json} y aun así se recorta de forma
 * defensiva por si acaso, igual que en el resto de adaptadores de este módulo.
 */
@Component
public class GeminiAiExtractionClient implements AiExtractionClientPort {

    private static final String MODEL = "gemini-3.6-flash";
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
            Eres un asistente que lee las notas tomadas durante o después de una reunión y extrae \
            elementos accionables para un tablero Kanban y una agenda personal.

            Devuelve ÚNICAMENTE un objeto JSON válido, sin texto adicional antes o después, sin \
            bloques de código markdown, con exactamente esta forma:
            {"tasks": ["..."], "checklistItems": ["..."], "summary": "..."}

            - "tasks": tareas sustanciales que merecen seguimiento en un tablero (títulos cortos y \
            claros, en español). Vacío si no hay ninguna.
            - "checklistItems": apuntes rápidos, recordatorios o pendientes menores que no merecen \
            ser una tarea de tablero (títulos cortos, en español). Vacío si no hay ninguno.
            - "summary": un resumen de una o dos frases de la reunión, en español, útil como nota \
            del día. Cadena vacía si no aporta nada que no esté ya en tasks/checklistItems.

            No inventes contenido que no esté sugerido por la nota. Si la nota está vacía o no \
            contiene nada accionable, devuelve listas vacías y summary vacío.
            """;

    private final RestClient restClient = RestClient.create();
    private final String apiKey;

    public GeminiAiExtractionClient(@Value("${app.gemini.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public ExtractionResult extract(String meetingTitle, String noteContent) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiExtractionException(
                    "No se ha configurado la API key de Gemini (variable de entorno GEMINI_API_KEY)", null);
        }

        String userPrompt = "Título de la reunión: " + meetingTitle + "\n\nNota:\n" + noteContent;
        GeminiRequest request = new GeminiRequest(
                List.of(new GeminiContent("user", List.of(new GeminiPart(userPrompt)))),
                new GeminiSystemInstruction(List.of(new GeminiPart(SYSTEM_PROMPT))),
                new GeminiGenerationConfig("application/json", 2048));

        try {
            GeminiResponse response = restClient.post()
                    .uri(ENDPOINT)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);
            return parse(extractText(response));
        } catch (RestClientException ex) {
            throw new AiExtractionException("Error al llamar a la API de Gemini", ex);
        }
    }

    private String extractText(GeminiResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new AiExtractionException("Respuesta de Gemini sin contenido", null);
        }
        GeminiResponseContent content = response.candidates().get(0).content();
        if (content == null || content.parts() == null || content.parts().isEmpty()) {
            throw new AiExtractionException("Respuesta de Gemini sin contenido", null);
        }
        return content.parts().stream().map(GeminiPart::text).filter(Objects::nonNull).collect(Collectors.joining());
    }

    private ExtractionResult parse(String text) {
        String json = extractJsonObject(text);
        try {
            ExtractionResponse parsed = OBJECT_MAPPER.readValue(json, ExtractionResponse.class);
            List<String> tasks = parsed.tasks() != null ? parsed.tasks() : List.of();
            List<String> checklistItems = parsed.checklistItems() != null ? parsed.checklistItems() : List.of();
            return new ExtractionResult(tasks, checklistItems, parsed.summary());
        } catch (JsonProcessingException ex) {
            throw new AiExtractionException("No se pudo interpretar la respuesta de Gemini", ex);
        }
    }

    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new AiExtractionException("La respuesta de Gemini no contenía JSON", null);
        }
        return text.substring(start, end + 1);
    }

    private record GeminiRequest(
            List<GeminiContent> contents, GeminiSystemInstruction systemInstruction,
            GeminiGenerationConfig generationConfig) {
    }

    private record GeminiContent(String role, List<GeminiPart> parts) {
    }

    private record GeminiSystemInstruction(List<GeminiPart> parts) {
    }

    private record GeminiPart(String text) {
    }

    private record GeminiGenerationConfig(String responseMimeType, int maxOutputTokens) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiResponse(List<GeminiCandidate> candidates) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiCandidate(GeminiResponseContent content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiResponseContent(List<GeminiPart> parts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ExtractionResponse(List<String> tasks, List<String> checklistItems, String summary) {
    }
}
