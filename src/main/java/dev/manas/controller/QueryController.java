package dev.manas.controller;

import dev.manas.service.AIService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Controller
public class QueryController {

    private final AIService aiService;

    public QueryController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/ask")
    public String ask(@RequestParam("query") String query, Model model) {
        model.addAttribute("query", query);

        // Updated to zip only two Mono streams
        var results = Mono.zip(
                aiService.callOpenAI(query),
                aiService.callGemini(query)
        ).block(Duration.ofSeconds(30));

        if (results != null) {
            model.addAttribute("openAIResponse", results.getT1());
            model.addAttribute("geminiResponse", results.getT2());
        }

        return "index";
    }
}
