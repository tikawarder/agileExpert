package hu.agilexpert.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import hu.agilexpert.exception.AiServiceException;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    public static final String COMMAND_SYSTEM_PROMPT =
        "You are an OS simulator assistant. Respond STRICTLY in valid JSON with DOUBLE QUOTES.\n" +
        "Format: { \"action\": \"START_APP\" | \"CHANGE_THEME\" | \"CREATE_USER\" | \"UNKNOWN\", \"target\": \"...\" }\n" +
        "Installed apps: [%s]\n" +
        "Current theme: %s\n" +
        "For CREATE_USER, 'target' should be the new user's name.";

    public static final String SIMULATION_SYSTEM_PROMPT =
        "Generate 3 new users, each with 2 unique applications (with name and icon name), and 1 unique theme.\n" +
        "The output must be STRICTLY valid JSON with no markdown, no code blocks, just raw JSON.\n" +
        "Format:\n" +
        "{\n" +
        "  \"users\": [\n" +
        "    {\n" +
        "      \"name\": \"Name1\",\n" +
        "      \"theme\": \"Theme1\",\n" +
        "      \"apps\": [\n" +
        "        { \"name\": \"App1\", \"icon\": \"Icon1\" },\n" +
        "        { \"name\": \"App2\", \"icon\": \"Icon2\" }\n" +
        "      ]\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper;

    public AiService() {
        this.objectMapper = new ObjectMapper();
        this.chatModel = buildChatModel();
    }

    private ChatLanguageModel buildChatModel() {
        String apiKey = loadApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            logger.warn("OPENAI_API_KEY is not set. AI features will not work.");
            return null;
        }
        String modelName = loadModelName();
        try {
            return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.3)
                .build();
        } catch (Exception e) {
            logger.error("Error initializing AI model: {}", e.getMessage());
            return null;
        }
    }

    private String loadApiKey() {
        try {
            return Dotenv.load().get("OPENAI_API_KEY");
        } catch (Exception e) {
            return System.getenv("OPENAI_API_KEY");
        }
    }

    private String loadModelName() {
        try {
            String name = Dotenv.load().get("OPENAI_MODEL_NAME");
            return (name != null && !name.isBlank()) ? name : "gpt-4o-mini";
        } catch (Exception e) {
            String name = System.getenv("OPENAI_MODEL_NAME");
            return (name != null && !name.isBlank()) ? name : "gpt-4o-mini";
        }
    }

    public JsonNode executePrompt(String systemMessage, String userMessage) {
        if (chatModel == null) {
            throw new AiServiceException("AI Model is not initialized. Check your API key.");
        }
        logger.debug("Executing AI prompt. User: {}", userMessage);
        String raw = chatModel.generate(List.of(
            SystemMessage.from(systemMessage),
            UserMessage.from(userMessage)
        )).content().text();
        String cleaned = raw.replaceAll("(?s)```json?\\s*|```", "").trim();
        try {
            return objectMapper.readTree(cleaned);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse AI response as JSON: {}", cleaned);
            throw new AiServiceException("Failed to parse AI response as JSON", e);
        }
    }

    public <T> T executePromptForDto(String systemMessage, String userMessage, Class<T> dtoClass) {
        JsonNode node = executePrompt(systemMessage, userMessage);
        try {
            return objectMapper.treeToValue(node, dtoClass);
        } catch (JsonProcessingException e) {
            logger.error("Failed to map AI response to {}: {}", dtoClass.getSimpleName(), e.getMessage());
            throw new AiServiceException("Invalid response format from AI", e);
        }
    }
}
