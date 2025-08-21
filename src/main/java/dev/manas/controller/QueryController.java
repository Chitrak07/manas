package dev.manas.controller;

import dev.manas.service.AIService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

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

        Mono<Tuple2<String, String>> resultsMono = Mono.zip(
                aiService.callOpenAI(query),
                aiService.callGemini(query)
        );

        Tuple2<String, String> results = resultsMono.block(Duration.ofSeconds(30));

        if (results != null) {
            model.addAttribute("openAIResponse", results.getT1());
            model.addAttribute("geminiResponse", results.getT2());
        }

        return "index";
    }
}
