package dev.manas.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.manas.service.AIService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Controller
public class QueryController {

    private final AIService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson's JSON parser

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

        Tuple2<String, String> rawResults = resultsMono.block(Duration.ofSeconds(60));

        if (rawResults != null) {
            Map<String, String> openAIResult = parseOpenAIResponse(rawResults.getT1());
            Map<String, String> geminiResult = parseGeminiResponse(rawResults.getT2());

            // Add both the formatted response and the model version to the Spring Model
            model.addAttribute("openAIResponse", convertMarkdownToHtml(openAIResult.get("text")));
            model.addAttribute("openAIModel", openAIResult.get("model"));
            model.addAttribute("geminiResponse", convertMarkdownToHtml(geminiResult.get("text")));
            model.addAttribute("geminiModel", geminiResult.get("model"));
        }

        return "index";
    }

    private Map<String, String> parseOpenAIResponse(String jsonResponse) {
        Map<String, String> result = new HashMap<>();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
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
            result.put("text", rootNode.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("Error parsing Gemini response."));
            String modelVersion = rootNode.path("candidates").path(0).path("content").path("role").asText("model");
            if (rootNode.has("modelVersion")) {
                modelVersion = rootNode.path("modelVersion").asText(modelVersion);
            }
            result.put("model", modelVersion);

        } catch (IOException e) {
            result.put("text", jsonResponse);
            result.put("model", "Error");
        }
        return result;
    }

    /**
     * A more robust markdown to HTML converter that handles headings, lists, tables, and bold text.
     * @param markdown The text with markdown characters.
     * @return A string with formatted HTML tags.
     */
    private String convertMarkdownToHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        String[] lines = markdown.split("\n");
        boolean inList = false;
        boolean inTable = false;
        boolean isTableHeader = true;

        for (String line : lines) {
            String trimmedLine = line.trim();

            // Handle Headings first as they are mutually exclusive
            if (trimmedLine.startsWith("####")) {
                if (inList) { html.append("</ul>"); inList = false; }
                if (inTable) { html.append("</table>"); inTable = false; }
                html.append("<h4>").append(trimmedLine.substring(4).trim()).append("</h4>");
                continue;
            }
            if (trimmedLine.startsWith("###")) {
                if (inList) { html.append("</ul>"); inList = false; }
                if (inTable) { html.append("</table>"); inTable = false; }
                html.append("<h3>").append(trimmedLine.substring(3).trim()).append("</h3>");
                continue;
            }
            if (trimmedLine.startsWith("##")) {
                if (inList) { html.append("</ul>"); inList = false; }
                if (inTable) { html.append("</table>"); inTable = false; }
                html.append("<h2>").append(trimmedLine.substring(2).trim()).append("</h2>");
                continue;
            }

            // Handle inline formatting for other lines
            String processedLine = trimmedLine.replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>");
            if (processedLine.startsWith("strong>")) {
                processedLine = "<strong>" + processedLine.substring(7).trim() + "</strong>";
            }

            // Handle List Items
            if (processedLine.startsWith("*")) {
                if (inTable) { html.append("</table>"); inTable = false; }
                if (!inList) {
                    inList = true;
                    html.append("<ul>");
                }
                html.append("<li>").append(processedLine.substring(1).trim()).append("</li>");
            }
            // Handle Tables
            else if (processedLine.startsWith("|") && processedLine.endsWith("|")) {
                if (inList) { html.append("</ul>"); inList = false; }
                if (!inTable) {
                    inTable = true;
                    isTableHeader = true; // First row of a new table is the header
                    html.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
                }

                // Check for the markdown table separator line and skip it
                if (processedLine.matches("\\|[:\\- ]+\\|?.*")) {
                    isTableHeader = false; // The next row will be the body
                    continue;
                }

                html.append("<tr>");
                String[] cells = processedLine.split("\\|");
                for (int i = 1; i < cells.length; i++) {
                    String cellContent = cells[i].trim();
                    if (isTableHeader) {
                        html.append("<th style='padding: 8px; text-align: left;'>").append(cellContent).append("</th>");
                    } else {
                        html.append("<td style='padding: 8px;'>").append(cellContent).append("</td>");
                    }
                }
                html.append("</tr>");
            }
            // Handle everything else
            else {
                if (inList) { html.append("</ul>"); inList = false; }
                if (inTable) { html.append("</table>"); inTable = false; }

                if (processedLine.isEmpty()) {
                    html.append("<br>");
                } else {
                    html.append("<p>").append(processedLine).append("</p>");
                }
            }
        }

        // Close any remaining open tags at the end of the text
        if (inList) { html.append("</ul>"); }
        if (inTable) { html.append("</table>"); }

        return html.toString();
    }
}
