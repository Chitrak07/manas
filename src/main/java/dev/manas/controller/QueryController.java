package dev.manas.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.manas.service.AIService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class QueryController {

    private final AIService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QueryController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        // Load existing chat history when the page is loaded
        List<Map<String, String>> chatHistory = (List<Map<String, String>>) session.getAttribute("chatHistory");
        if (chatHistory != null) {
            model.addAttribute("chatHistory", chatHistory);
        }
        return "index";
    }

    @GetMapping("/new-chat")
    public String newChat(HttpSession session) {
        // Clear the history from the session
        session.removeAttribute("chatHistory");
        return "redirect:/";
    }

    @PostMapping("/ask")
    public String ask(@RequestParam("query") String query, @RequestParam("model") String modelSelection, HttpSession session, Model model) {
        // 1. Get or create chat history from the session
        List<Map<String, String>> chatHistory = (List<Map<String, String>>) session.getAttribute("chatHistory");
        if (chatHistory == null) {
            chatHistory = new ArrayList<>();
        }

        // 2. Add the new user query to the history
        chatHistory.add(Map.of("role", "user", "content", query));

        boolean callOpenAI = modelSelection.equals("both") || modelSelection.equals("openai");
        boolean callGemini = modelSelection.equals("both") || modelSelection.equals("gemini");

        Mono<String> openAIResponseMono = callOpenAI ? aiService.callOpenAI(chatHistory) : Mono.just("{\"model\":\"N/A\", \"text\":\"Not called\"}");
        Mono<String> geminiResponseMono = callGemini ? aiService.callGemini(chatHistory) : Mono.just("{\"model\":\"N/A\", \"text\":\"Not called\"}");

        Tuple2<String, String> rawResults = Mono.zip(openAIResponseMono, geminiResponseMono)
                .block(Duration.ofSeconds(60));

        // 3. Process responses and add them to the history
        if (rawResults != null) {
            if (callOpenAI) {
                Map<String, String> openAIResult = parseOpenAIResponse(rawResults.getT1());
                chatHistory.add(Map.of("role", "assistant", "content", openAIResult.get("text"), "model", "OpenAI: " + openAIResult.get("model")));
            }
            if (callGemini) {
                Map<String, String> geminiResult = parseGeminiResponse(rawResults.getT2());
                chatHistory.add(Map.of("role", "assistant", "content", geminiResult.get("text"), "model", "Gemini: " + geminiResult.get("model")));
            }
        }

        // 4. Save updated history to session and model
        session.setAttribute("chatHistory", chatHistory);
        model.addAttribute("chatHistory", chatHistory);

        return "index";
    }

    private Map<String, String> parseOpenAIResponse(String jsonResponse) {
        Map<String, String> result = new HashMap<>();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            if (rootNode.has("error")) {
                result.put("text", rootNode.get("error").asText());
                result.put("model", "Error");
                return result;
            }
            result.put("text", rootNode.path("choices").path(0).path("message").path("content").asText("Error parsing OpenAI response."));
            result.put("model", rootNode.path("model").asText("N/A"));
        } catch (IOException e) {
            result.put("text", jsonResponse);
            result.put("model", "Error");
        }
        return result;
    }

    private Map<String, String> parseGeminiResponse(String jsonResponse) {
        Map<String, String> result = new HashMap<>();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            if (rootNode.has("error")) {
                result.put("text", rootNode.get("error").asText());
                result.put("model", "Error");
                return result;
            }
            result.put("text", rootNode.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("Error parsing Gemini response."));
            result.put("model", "gemini-pro");
        } catch (IOException e) {
            result.put("text", jsonResponse);
            result.put("model", "Error");
        }
        return result;
    }
}
