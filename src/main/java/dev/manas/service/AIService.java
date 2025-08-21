package dev.manas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", query);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini");
        body.put("messages", Collections.singletonList(message));

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
        Map<String, Object> part = new HashMap<>();
        part.put("text", query);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", Collections.singletonList(part));

        Map<String, Object> body = new HashMap<>();
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
