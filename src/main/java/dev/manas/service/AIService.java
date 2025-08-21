package dev.manas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIService {

    private final WebClient webClient;

    @Value("${api.openai.url}")
    private String openAIUrl;
    @Value("${api.openai.key}")
    private String openAIKey;

    @Value("${api.gemini.key}")
    private String geminiKey;

    public AIService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    // Updated to accept a list of messages for conversation history
    public Mono<String> callOpenAI(List<Map<String, String>> messages) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini");
        body.put("messages", messages); // Send the whole conversation

        return webClient.post()
                .uri(openAIUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAIKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just("{\"error\": \"Error calling OpenAI: " + e.getMessage() + "\"}"))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // Updated to accept a list of messages for conversation history
    public Mono<String> callGemini(List<Map<String, String>> messages) {
        // Gemini API requires a specific format for contents
        List<Map<String, Object>> contents = messages.stream()
                .map(message -> {
                    Map<String, Object> part = new HashMap<>();
                    part.put("text", message.get("content"));
                    Map<String, Object> content = new HashMap<>();
                    content.put("parts", List.of(part));
                    // Gemini uses 'user' and 'model' for roles
                    content.put("role", message.get("role").equals("assistant") ? "model" : message.get("role"));
                    return content;
                })
                .collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);

        String finalUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent?key=" + geminiKey;

        return webClient.post()
                .uri(finalUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just("{\"error\": \"Error calling Gemini: " + e.getMessage() + "\"}"))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
