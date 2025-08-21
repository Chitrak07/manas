package dev.manas.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.manas.service.AIService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class QueryController {

    private final AIService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QueryController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        Map<String, Object> chatSession = getOrCreateChatSession(session);
        Map<String, List<Map<String, String>>> allChats = (Map<String, List<Map<String, String>>>) chatSession.get("allChats");
        String activeChatId = (String) chatSession.get("activeChatId");

        model.addAttribute("allChats", allChats);
        model.addAttribute("activeChatId", activeChatId);
        if (activeChatId != null) {
            model.addAttribute("chatHistory", allChats.get(activeChatId));
        }

        return "index";
    }

    @GetMapping("/new-chat")
    public String newChat(HttpSession session) {
        Map<String, Object> chatSession = getOrCreateChatSession(session);
        Map<String, List<Map<String, String>>> allChats = (Map<String, List<Map<String, String>>>) chatSession.get("allChats");

        String newChatId = UUID.randomUUID().toString();
        allChats.put(newChatId, new ArrayList<>());
        chatSession.put("activeChatId", newChatId);

        session.setAttribute("chatSession", chatSession);
        return "redirect:/";
    }

    @GetMapping("/chat/{chatId}")
    public String switchChat(@PathVariable String chatId, HttpSession session) {
        Map<String, Object> chatSession = getOrCreateChatSession(session);
        Map<String, List<Map<String, String>>> allChats = (Map<String, List<Map<String, String>>>) chatSession.get("allChats");

        if (allChats.containsKey(chatId)) {
            chatSession.put("activeChatId", chatId);
            session.setAttribute("chatSession", chatSession);
        }
        return "redirect:/";
    }

    @PostMapping("/ask")
    public String ask(@RequestParam("query") String query, @RequestParam("model") String modelSelection, HttpSession session) {
        Map<String, Object> chatSession = getOrCreateChatSession(session);
        Map<String, List<Map<String, String>>> allChats = (Map<String, List<Map<String, String>>>) chatSession.get("allChats");
        String activeChatId = (String) chatSession.get("activeChatId");

        if (activeChatId == null || !allChats.containsKey(activeChatId)) {
            activeChatId = UUID.randomUUID().toString();
            allChats.put(activeChatId, new ArrayList<>());
            chatSession.put("activeChatId", activeChatId);
        }

        List<Map<String, String>> activeChatHistory = allChats.get(activeChatId);
        activeChatHistory.add(Map.of("role", "user", "content", query));

        boolean callOpenAI = modelSelection.equals("both") || modelSelection.equals("openai");
        boolean callGemini = modelSelection.equals("both") || modelSelection.equals("gemini");

        Mono<String> openAIResponseMono = callOpenAI ? aiService.callOpenAI(activeChatHistory) : Mono.just("{\"model\":\"N/A\", \"text\":\"Not called\"}");
        Mono<String> geminiResponseMono = callGemini ? aiService.callGemini(activeChatHistory) : Mono.just("{\"model\":\"N/A\", \"text\":\"Not called\"}");

        Tuple2<String, String> rawResults = Mono.zip(openAIResponseMono, geminiResponseMono).block(Duration.ofSeconds(60));

        if (rawResults != null) {
            if (callOpenAI) {
                Map<String, String> result = parseOpenAIResponse(rawResults.getT1());
                activeChatHistory.add(Map.of("role", "assistant", "content", result.get("text"), "model", "OpenAI: " + result.get("model")));
            }
            if (callGemini) {
                Map<String, String> result = parseGeminiResponse(rawResults.getT2());
                activeChatHistory.add(Map.of("role", "assistant", "content", result.get("text"), "model", "Gemini: " + result.get("model")));
            }
        }

        session.setAttribute("chatSession", chatSession);
        return "redirect:/";
    }

    private Map<String, Object> getOrCreateChatSession(HttpSession session) {
        Map<String, Object> chatSession = (Map<String, Object>) session.getAttribute("chatSession");
        if (chatSession == null) {
            chatSession = new HashMap<>();
            // Use LinkedHashMap to preserve insertion order for the history list
            chatSession.put("allChats", new LinkedHashMap<String, List<Map<String, String>>>());
            chatSession.put("activeChatId", null);
            session.setAttribute("chatSession", chatSession);
        }
        return chatSession;
    }

    private Map<String, String> parseOpenAIResponse(String jsonResponse) {
        Map<String, String> result = new HashMap<>();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            if (rootNode.has("error")) {
                result.put("text", rootNode.path("error").asText("Unknown error from OpenAI."));
                result.put("model", "Error");
                return result;
            }
            result.put("text", rootNode.path("choices").path(0).path("message").path("content").asText("No content found in OpenAI response."));
            result.put("model", rootNode.path("model").asText("N/A"));
        } catch (IOException e) {
            result.put("text", jsonResponse); // Return the raw string if it's not valid JSON
            result.put("model", "Parsing Error");
        }
        return result;
    }

    private Map<String, String> parseGeminiResponse(String jsonResponse) {
        Map<String, String> result = new HashMap<>();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            if (rootNode.has("error")) {
                result.put("text", rootNode.path("error").asText("Unknown error from Gemini."));
                result.put("model", "Error");
                return result;
            }
            result.put("text", rootNode.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("No content found in Gemini response."));
            result.put("model", "gemini-pro");
        } catch (IOException e) {
            result.put("text", jsonResponse);
            result.put("model", "Parsing Error");
        }
        return result;
    }
}
