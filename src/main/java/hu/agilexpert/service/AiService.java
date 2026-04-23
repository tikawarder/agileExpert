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

public class AiService {

    private ChatLanguageModel chatModel;
    private ObjectMapper objectMapper;
    private DbService dbService;

    public AiService() {
        this.objectMapper = new ObjectMapper();
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

    public void runSimulation() {
        System.out.println("LLM Szimulációs adatok generálása folyamatban (ez eltarthat egy ideig)...");
        String systemMsg = "Generálj 3 új felhasználót, mindegyikhez 2 egyedi alkalmazást (névvel és ikon névvel), és 1 egyedi témát.\n" +
            "A kimenet Szigorúan csak egy valid JSON legyen, markdown kódblokk (```json) NÉLKÜL!\n" +
            "Formátum:\n" +
            "{\n" +
            "  \"users\": [\n" +
            "    {\n" +
            "      \"name\": \"Nev1\",\n" +
            "      \"theme\": \"Tema1\",\n" +
            "      \"apps\": [\n" +
            "        { \"name\": \"App1\", \"icon\": \"Icon1\" },\n" +
            "        { \"name\": \"App2\", \"icon\": \"Icon2\" }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        
        JsonNode response = executePrompt(systemMsg, "Kérlek generáld le a szimulációs adatokat.");
        
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
                System.out.println("=> Sikeresen generálva és mentve " + userCount + " felhasználó és " + appCount + " alkalmazás!");
            });
        } else {
            System.out.println("Hiba történt a generálás során. Az LLM nem érvényes JSON-t adott vissza.");
        }
    }
}
