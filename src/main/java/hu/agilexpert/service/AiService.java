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
            logger.error("AI Model is not initialized.");
            return null;
        }
        try {
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
            return null;
        }
    }

    public void runSimulation() {
        System.out.println("Generating LLM simulation data (this may take a while)...");
        String systemMsg = "Generate 3 new users, each with 2 unique applications (with name and icon name), and 1 unique theme.\n" +
            "The output must be STRICTLY valid JSON, WITHOUT markdown code blocks (```json)!\n" +
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
        
        JsonNode response = executePrompt(systemMsg, "Please generate simulation data.");
        
        if (response != null && response.has("users")) {
            dbService.inTransaction(em -> {
                int userCount = 0;
                int appCount = 0;

                for (JsonNode userNode : response.get("users")) {
                    String uName = userNode.get("name").asText();
                    String tName = userNode.get("theme").asText();
                    
                    Theme theme = new Theme(tName);
                    em.persist(theme);
                    
                    UserAccount u = new UserAccount(uName);
                    u.setTheme(theme);
                    u.setDeviceMenu(new Menu(uName + "'s Menu"));
                    
                    if (userNode.has("apps")) {
                        for (JsonNode appNode : userNode.get("apps")) {
                            String aName = appNode.get("name").asText();
                            String iName = appNode.get("icon").asText();
                            
                            Icon icon = new Icon(iName);
                            em.persist(icon);
                            
                            App app = new App(aName, icon);
                            em.persist(app);
                            
                            u.getInstalledApps().add(app);
                            appCount++;
                        }
                    }
                    
                    em.persist(u);
                    userCount++;
                }
                System.out.println("=> Successfully generated and saved " + userCount + " users and " + appCount + " applications!");
            });
        } else {
            System.out.println("An error occurred during generation. The LLM did not return a valid JSON.");
        }
    }
}
