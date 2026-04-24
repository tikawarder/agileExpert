package hu.agilexpert.service;

import hu.agilexpert.dto.SimulationDataDto;
import hu.agilexpert.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulationService {
    private static final Logger logger = LoggerFactory.getLogger(SimulationService.class);
    private final AiService aiService;
    private final DbService dbService;

    public SimulationService() {
        this.aiService = new AiService();
        this.dbService = DbService.getInstance();
    }

    public void runSimulation() {
        logger.info("Starting LLM simulation data generation...");
        String systemMsg = "Generate 3 new users, each with 2 unique applications (with name and icon name), and 1 unique theme.\n" +
                "The output must be STRICTLY valid JSON!\n" +
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

        SimulationDataDto data = aiService.executePromptForDto(systemMsg, "Please generate simulation data.", SimulationDataDto.class);

        if (data != null && data.getUsers() != null) {
            dbService.inTransaction(em -> {
                int userCount = 0;
                int appCount = 0;

                for (SimulationDataDto.UserDto userDto : data.getUsers()) {
                    Theme theme = new Theme(userDto.getTheme());
                    em.persist(theme);

                    UserAccount u = new UserAccount(userDto.getName());
                    u.setTheme(theme);
                    u.setDeviceMenu(new Menu(userDto.getName() + "'s Menu"));

                    if (userDto.getApps() != null) {
                        for (SimulationDataDto.AppDto appDto : userDto.getApps()) {
                            Icon icon = new Icon(appDto.getIcon());
                            em.persist(icon);

                            App app = new App(appDto.getName(), icon);
                            em.persist(app);

                            u.getInstalledApps().add(app);
                            appCount++;
                        }
                    }

                    em.persist(u);
                    userCount++;
                }
                logger.info("Successfully generated and saved {} users and {} applications!", userCount, appCount);
                System.out.println("=> Successfully generated and saved " + userCount + " users and " + appCount + " applications!");
            });
        }
    }
}
