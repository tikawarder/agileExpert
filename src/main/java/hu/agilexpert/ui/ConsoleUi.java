package hu.agilexpert.ui;

import com.fasterxml.jackson.databind.JsonNode;
import hu.agilexpert.model.*;
import hu.agilexpert.service.AiService;
import hu.agilexpert.service.DbService;
import hu.agilexpert.service.OsService;
import hu.agilexpert.service.UserService;
import hu.agilexpert.service.SimulationService;
import hu.agilexpert.exception.AgileExpertException;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Scanner;

public class ConsoleUi {
    private final UserService userService;
    private final OsService osService;
    private final AiService aiService;
    private final SimulationService simulationService;
    private UserAccount currentUser;
    private final Scanner scanner;

    public ConsoleUi() {
        this.userService = new UserService();
        this.osService = new OsService();
        this.aiService = new AiService();
        this.simulationService = new SimulationService();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Initializing Database and populating initial data...");
        populateInitialData();

        System.out.println("Welcome to the AgileXpert OS simulator!");

        boolean running = true;
        while (running) {
            printMainMenu();
            String input = scanner.nextLine();
            switch (input) {
                case "1" -> createUser();
                case "2" -> selectUser();
                case "3" -> showState();
                case "4" -> modifyMenu();
                case "5" -> startApp();
                case "6" -> changeTheme();
                case "7" -> setBackground();
                case "8" -> manageIcons();
                case "9" -> installApp();
                case "10" -> executeAiCommand();
                case "11" -> runSimulation();
                case "0" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
        System.out.println("Goodbye!");
    }

    private void printMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Create User");
        System.out.println("2. Select User");
        System.out.println("3. Show Current User State");
        System.out.println("4. Modify Current User's App Menu");
        System.out.println("5. Start Application");
        System.out.println("6. Change Theme");
        System.out.println("7. Set Background Image");
        System.out.println("8. Manage Icons");
        System.out.println("9. Install App from Store");
        System.out.println("10. AI Assistant (Natural Language Control)");
        System.out.println("11. Run System Simulation (LLM Data Generation)");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private void populateInitialData() {
        UserAccount initialUser = userService.findUserByName("Tamas Biro");
        if (initialUser != null) {
            currentUser = initialUser;
            System.out.println("Persistent data found. Loaded user: " + currentUser.getName());
            return;
        }

        System.out.println("No existing data found. Creating default setup...");
        DbService.getInstance().inTransaction(em -> {
            Icon defaultIcon = new Icon("Default App Icon");
            App app1 = new App("Minesweeper", defaultIcon);
            App app2 = new App("OpenMap", defaultIcon);
            App app3 = new App("Paint", defaultIcon);
            App app4 = new App("Directory", defaultIcon);
            
            Theme defaultTheme = new Theme("Light Theme");
            BackgroundImage defaultBgm = new BackgroundImage("Blue Mountains");
            
            em.persist(defaultIcon);
            em.persist(app1);
            em.persist(app2);
            em.persist(app3);
            em.persist(app4);
            em.persist(defaultTheme);
            em.persist(defaultBgm);

            UserAccount user = new UserAccount("Tamas Biro");
            user.setTheme(defaultTheme);
            user.setBackgroundImage(defaultBgm);

            Menu myMenu = new Menu("Tamas Biro's Main Menu");
            myMenu.getItems().add(new MenuItem("Game: Minesweeper", app1));
            myMenu.getItems().add(new MenuItem("Navigation: OpenMap", app2));
            myMenu.getItems().add(new MenuItem("Drawing: Paint", app3));
            myMenu.getItems().add(new MenuItem("Contacts: Directory", app4));

            user.setDeviceMenu(myMenu);
            em.persist(user);
            currentUser = user;
        });
    }

    private void createUser() {
        System.out.print("Enter new user's name (0 to go back): ");
        String name = scanner.nextLine();
        if ("0".equals(name) || name.isBlank()) return;
        UserAccount user = userService.createUser(name);
        System.out.println("Created User: " + user.getName());
    }

    private void selectUser() {
        List<UserAccount> users = userService.getAllUsers();
        System.out.println("Available users:");
        for (int i = 0; i < users.size(); i++) {
            System.out.println((i + 1) + ". " + users.get(i).getName());
        }
        System.out.println("0. Back");
        System.out.print("Select user by number: ");
        try {
            int num = Integer.parseInt(scanner.nextLine());
            if (num > 0 && num <= users.size()) {
                currentUser = users.get(num - 1);
                System.out.println("Selected " + currentUser.getName());
            }
        } catch (Exception e) {
            System.err.println("Error selecting user: " + e.getMessage());
        }
    }

    private void showState() {
        if (currentUser == null) {
            System.out.println("No user selected.");
            return;
        }
        System.out.println("--- Current User State ---");
        System.out.println("Name: " + currentUser.getName());
        System.out.println("Theme: " + (currentUser.getTheme() != null ? currentUser.getTheme().getName() : "None"));
        System.out.println("Background: " + (currentUser.getBackgroundImage() != null ? currentUser.getBackgroundImage().getName() : "None"));
        System.out.println("Menu Items:");
        if (currentUser.getDeviceMenu() != null) {
            printMenuRecursively(currentUser.getDeviceMenu(), 1);
        }
        System.out.println("Installed Apps:");
        currentUser.getInstalledApps().forEach(a -> System.out.println("  - " + a.getName()));
    }

    private void printMenuRecursively(Menu menu, int depth) {
        String indent = "  ".repeat(depth);
        if (menu.getItems().isEmpty()) System.out.println(indent + "(Empty)");
        for (MenuItem item : menu.getItems()) {
            if (item.getApp() != null) {
                System.out.println(indent + "- [App] " + item.getLabel() + " -> " + item.getApp().getName());
            } else if (item.getSubMenu() != null) {
                System.out.println(indent + "- [SubMenu] " + item.getLabel());
                printMenuRecursively(item.getSubMenu(), depth + 1);
            }
        }
    }

    private void modifyMenu() {
        if (currentUser == null) return;
        System.out.println("1. Add application to menu\n2. Add submenu\n0. Back");
        String opt = scanner.nextLine();
        if (opt.equals("1")) {
            List<App> apps = osService.getAllApps();
            for (int i = 0; i < apps.size(); i++) System.out.println((i + 1) + ". " + apps.get(i).getName());
            try {
                int num = Integer.parseInt(scanner.nextLine());
                if (num > 0 && num <= apps.size()) {
                    System.out.print("Label: ");
                    osService.addAppToMenu(currentUser, apps.get(num - 1), scanner.nextLine());
                }
            } catch (Exception e) {
                System.err.println("Error adding app: " + e.getMessage());
            }
        } else if (opt.equals("2")) {
            System.out.print("Label: ");
            osService.addSubMenu(currentUser, scanner.nextLine());
        }
    }

    private void startApp() {
        System.out.println("Launching App simulation...");
    }

    private void changeTheme() {
        if (currentUser == null) return;
        System.out.print("New theme name: ");
        osService.setTheme(currentUser, scanner.nextLine());
    }

    private void setBackground() {
        if (currentUser == null) return;
        System.out.print("New background name: ");
        osService.setBackground(currentUser, scanner.nextLine());
    }

    private void manageIcons() {
        System.out.println("1. List Icons\n2. Add Icon\n0. Back");
        String opt = scanner.nextLine();
        if (opt.equals("1")) osService.getAllIcons().forEach(i -> System.out.println(i.getName()));
        else if (opt.equals("2")) {
            System.out.print("Icon name: ");
            osService.addIcon(scanner.nextLine());
        }
    }

    private void installApp() {
        if (currentUser == null) return;
        List<App> apps = osService.getAllApps();
        for (int i = 0; i < apps.size(); i++) System.out.println((i + 1) + ". " + apps.get(i).getName());
        try {
            int num = Integer.parseInt(scanner.nextLine());
            if (num > 0 && num <= apps.size()) osService.installApp(currentUser, apps.get(num - 1));
        } catch (Exception e) {
            System.err.println("Error installing app: " + e.getMessage());
        }
    }

    private void executeAiCommand() {
        if (currentUser == null) return;
        System.out.print("Command: ");
        String request = scanner.nextLine();
        
        try {
            StringBuilder appsStr = new StringBuilder();
            currentUser.getInstalledApps().forEach(a -> appsStr.append(a.getName()).append(", "));

            String systemMsg = "You are an OS simulator assistant. Respond STRICTLY in valid JSON with DOUBLE QUOTES: { \"action\": \"START_APP\"/\"CHANGE_THEME\"/\"CREATE_USER\"/\"UNKNOWN\", \"target\": \"...\" }\n" +
                    "Apps: [" + appsStr + "]\nTheme: " + (currentUser.getTheme() != null ? currentUser.getTheme().getName() : "None") + "\n" +
                    "For CREATE_USER, 'target' should be the name of the new user.";

            JsonNode resp = aiService.executePrompt(systemMsg, request);
            if (resp != null && resp.has("action")) {
                String act = resp.get("action").asText();
                String trg = resp.get("target").asText();
                if ("START_APP".equals(act)) {
                    System.out.println("[AI] Starting application: " + trg);
                } else if ("CHANGE_THEME".equals(act)) {
                    osService.setTheme(currentUser, trg);
                    System.out.println("[AI] Theme changed to: " + trg);
                } else if ("CREATE_USER".equals(act)) {
                    UserAccount newUser = userService.createUser(trg);
                    System.out.println("[AI] Created new user: " + newUser.getName());
                } else {
                    System.out.println("[AI] I'm sorry, I don't know how to perform this action yet: " + request);
                }
            } else {
                System.out.println("[AI] I couldn't understand that command. Please try something like 'Start Minesweeper' or 'Create user Ivan'.");
            }
        } catch (AgileExpertException e) {
            System.err.println("[AI ERROR] " + e.getMessage());
        }
    }

    private void runSimulation() {
        try {
            simulationService.runSimulation();
        } catch (AgileExpertException e) {
            System.err.println("[SIMULATION ERROR] " + e.getMessage());
        }
    }
}
