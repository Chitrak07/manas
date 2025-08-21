package dev.manas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap; // Import HashMap
import java.util.List;
import java.util.Map;
import java.util.Collections; // Import Collections

@Service
public class AIService {

    private final WebClient webClient;

    @Value("${api.openai.url}")
    private String openAIUrl;
    @Value("${api.openai.key}")
    private String openAIKey;

    @Value("${api.gemini.url}")
    private String geminiUrl;
    @Value("${api.gemini.key}")
    private String geminiKey;

    public AIService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<String> callOpenAI(String query) {
        // Replaced Map.of() with a Java 8 compatible method
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini");
        body.put("messages", Collections.singletonList(Collections.singletonMap("role", "user")));
        // A more robust way to add the content to the nested map
        ((List<Map<String, String>>) body.get("messages")).get(0).put("content", query);


        return webClient.post()
                .uri(openAIUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAIKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just("Error calling OpenAI: " + e.getMessage()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> callGemini(String query) {
        // Replaced Map.of() with a Java 8 compatible method
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", query);
        Map<String, Object> content = new HashMap<>();
        content.put("parts", Collections.singletonList(part));
        body.put("contents", Collections.singletonList(content));

        String finalUrl = geminiUrl + "?key=" + geminiKey;

        return webClient.post()
                .uri(finalUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just("Error calling Gemini: " + e.getMessage()))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
