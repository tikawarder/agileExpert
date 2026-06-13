package hu.agilexpert.service;

import hu.agilexpert.dto.SimulationDataDto;
import hu.agilexpert.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulationService {
    private static final Logger logger = LoggerFactory.getLogger(SimulationService.class);
    private final AiService aiService;
    private final DbService dbService;

    public SimulationService(AiService aiService) {
        this.aiService = aiService;
        this.dbService = DbService.getInstance();
    }

    public void runSimulation() {
        logger.info("Starting LLM simulation data generation...");
        SimulationDataDto data = aiService.executePromptForDto(
            AiService.SIMULATION_SYSTEM_PROMPT,
            "Please generate simulation data.",
            SimulationDataDto.class
        );

        if (data != null && data.getUsers() != null) {
            dbService.inTransaction(em -> {
                int[] counts = {0, 0};
                for (SimulationDataDto.UserDto userDto : data.getUsers()) {
                    persistUserFromDto(em, userDto, counts);
                }
                logger.info("Successfully generated and saved {} users and {} applications!", counts[0], counts[1]);
                System.out.println("=> Successfully generated and saved " + counts[0] + " users and " + counts[1] + " applications!");
            });
        }
    }

    private void persistUserFromDto(jakarta.persistence.EntityManager em, SimulationDataDto.UserDto userDto, int[] counts) {
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
                counts[1]++;
            }
        }
        em.persist(u);
        counts[0]++;
    }
}
