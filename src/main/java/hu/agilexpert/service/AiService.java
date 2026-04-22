package hu.agilexpert.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.github.cdimascio.dotenv.Dotenv;

public class AiService {

    private ChatLanguageModel chatModel;
    private ObjectMapper objectMapper;

    public AiService() {
        this.objectMapper = new ObjectMapper();
        
        // Load API key from .env file
        String apiKey = null;
        try {
            Dotenv dotenv = Dotenv.load();
            apiKey = dotenv.get("OPENAI_API_KEY");
        } catch (Exception e) {
            // Fallback if .env is missing
            apiKey = System.getenv("OPENAI_API_KEY");
        }

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("[FIGYELMEZTETÉS] Nincs beállítva az OPENAI_API_KEY! Az AI funkciók nem fognak működni.");
        } else {
            try {
                this.chatModel = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName("gpt-3.5-turbo")
                    .temperature(0.3) // Lower temperature for more deterministic JSON outputs
                    .build();
            } catch (Exception e) {
                System.err.println("Hiba az AI modell inicializálásakor: " + e.getMessage());
            }
        }
    }

    public JsonNode executePrompt(String systemMessage, String userMessage) {
        if (chatModel == null) {
            System.err.println("AI Model is not initialized.");
            return null;
        }
        try {
            String prompt = systemMessage + "\n\nUtasítás: " + userMessage;
            String response = chatModel.generate(prompt);
            
            // Clean markdown blocks if the model wrapped it in ```json ... ```
            if (response.startsWith("```json")) {
                response = response.substring(7);
            }
            if (response.startsWith("```")) {
                response = response.substring(3);
            }
            if (response.endsWith("```")) {
                response = response.substring(0, response.length() - 3);
            }
            
            return objectMapper.readTree(response.trim());
        } catch (Exception e) {
            System.err.println("Hiba az AI hívás vagy feldolgozás közben: " + e.getMessage());
            return null;
        }
    }
}
