package hu.agilexpert.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.github.cdimascio.dotenv.Dotenv;

import hu.agilexpert.model.App;
import hu.agilexpert.model.Icon;
import hu.agilexpert.model.Menu;
import hu.agilexpert.model.Theme;
import hu.agilexpert.model.UserAccount;
import hu.agilexpert.exception.AiServiceException;
import hu.agilexpert.dto.SimulationDataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);
    private ChatLanguageModel chatModel;
    private ObjectMapper objectMapper;
    private DbService dbService;

    public AiService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        this.dbService = DbService.getInstance();
        
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
            logger.warn("OPENAI_API_KEY is not set! AI features will not work.");
        } else {
            try {
                this.chatModel = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName("gpt-3.5-turbo")
                    .temperature(0.3) // Lower temperature for more deterministic JSON outputs
                    .build();
            } catch (Exception e) {
                logger.error("Error initializing AI model: {}", e.getMessage());
            }
        }
    }

    public JsonNode executePrompt(String systemMessage, String userMessage) {
        if (chatModel == null) {
            throw new AiServiceException("AI Model is not initialized. Check your API key.");
        }
        try {
            logger.debug("Executing AI prompt. System: {}, User: {}", systemMessage, userMessage);
            String prompt = systemMessage + "\n\nInstruction: " + userMessage;
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
            logger.error("Error during AI call or processing: {}", e.getMessage());
            throw new AiServiceException("Failed to process AI command", e);
        }
    }

    public <T> T executePromptForDto(String systemMessage, String userMessage, Class<T> dtoClass) {
        JsonNode node = executePrompt(systemMessage, userMessage);
        try {
            return objectMapper.treeToValue(node, dtoClass);
        } catch (Exception e) {
            logger.error("Failed to map AI response to DTO {}: {}", dtoClass.getSimpleName(), e.getMessage());
            throw new AiServiceException("Invalid response format from AI", e);
        }
    }

}
