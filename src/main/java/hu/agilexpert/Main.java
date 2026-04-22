package hu.agilexpert;

import hu.agilexpert.model.*;
import hu.agilexpert.service.AiService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Scanner;

public class Main {
    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static UserAccount currentUser;
    private static AiService aiService;

    public static void main(String[] args) {
        emf = Persistence.createEntityManagerFactory("agilexpert-pu");
        em = emf.createEntityManager();
        aiService = new AiService();

        System.out.println("Initializing Database and populating initial data...");
        populateInitialData();

        System.out.println("Welcome to the AgileXpert OS simulator!");
        
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
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
            System.out.println("10. AI Asszisztens (Természetes nyelvű vezérlés)");
            System.out.println("11. Rendszer Szimuláció futtatása (LLM adatgenerálás)");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine();
            switch (input) {
                case "1":
                    createUser(scanner);
                    break;
                case "2":
                    selectUser(scanner);
                    break;
                case "3":
                    showState();
                    break;
                case "4":
                    modifyMenu(scanner);
                    break;
                case "5":
                    startApp(scanner);
                    break;
                case "6":
                    changeTheme(scanner);
                    break;
                case "7":
                    setBackground(scanner);
                    break;
                case "8":
                    manageIcons(scanner);
                    break;
                case "9":
                    installApp(scanner);
                    break;
                case "10":
                    executeAiCommand(scanner);
                    break;
                case "11":
                    runAiSimulation();
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }

        em.close();
        emf.close();
        System.out.println("Goodbye!");
    }

    private static void populateInitialData() {
        // Check if data already exists (Admin is our marker)
        var existing = em.createQuery("SELECT u FROM UserAccount u WHERE u.name = 'Admin'", UserAccount.class)
                .getResultList();
        
        if (!existing.isEmpty()) {
            currentUser = existing.get(0);
            System.out.println("Persistent data found. Loaded user: " + currentUser.getName());
            return;
        }

        System.out.println("No existing data found. Creating default setup...");
        em.getTransaction().begin();

        Icon defaultIcon = new Icon("Default Disk Icon");
        App app1 = new App("File Manager", defaultIcon);
        App app2 = new App("Browser", defaultIcon);
        App app3 = new App("Terminal", defaultIcon);
        
        Theme defaultTheme = new Theme("Dark Mode");
        BackgroundImage defaultBgm = new BackgroundImage("Starry Night");
        
        em.persist(defaultIcon);
        em.persist(app1);
        em.persist(app2);
        em.persist(app3);
        em.persist(defaultTheme);
        em.persist(defaultBgm);

        UserAccount admin = new UserAccount("Admin");
        admin.setTheme(defaultTheme);
        admin.setBackgroundImage(defaultBgm);

        Menu myMenu = new Menu("Admin's Root Menu");
        myMenu.getItems().add(new MenuItem("Open File Manager", app1));
        myMenu.getItems().add(new MenuItem("Open Browser", app2));
        myMenu.getItems().add(new MenuItem("Open Terminal", app3));

        admin.setDeviceMenu(myMenu);

        em.persist(admin);

        em.getTransaction().commit();
        currentUser = admin;
    }

    private static void createUser(Scanner scanner) {
        System.out.print("Enter new user's name (0 to go back): ");
        String name = scanner.nextLine();
        if ("0".equals(name) || "back".equalsIgnoreCase(name) || name.isBlank()) return;
        
        em.getTransaction().begin();
        UserAccount newUser = new UserAccount(name);
        Menu menu = new Menu(name + "'s Root Menu");
        newUser.setDeviceMenu(menu);
        em.persist(newUser);
        em.getTransaction().commit();

        System.out.println("Created User: " + newUser.getName());
    }

    private static void selectUser(Scanner scanner) {
        var users = em.createQuery("SELECT u FROM UserAccount u", UserAccount.class).getResultList();
        System.out.println("Available users:");
        for (int i = 0; i < users.size(); i++) {
            System.out.println((i + 1) + ". " + users.get(i).getName());
        }
        System.out.println("0. Back");
        System.out.print("Select user by number: ");
        try {
            int num = Integer.parseInt(scanner.nextLine());
            if (num == 0) {
                return;
            } else if (num > 0 && num <= users.size()) {
                currentUser = users.get(num - 1);
                System.out.println("Selected " + currentUser.getName());
            } else {
                System.out.println("Invalid selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private static void showState() {
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
            printMenu(currentUser.getDeviceMenu(), 1);
        }
        System.out.println("Installed Apps (ManyToMany):");
        if (currentUser.getInstalledApps().isEmpty()) {
            System.out.println("  (None)");
        } else {
            for (App app : currentUser.getInstalledApps()) {
                System.out.println("  - " + app.getName());
            }
        }
    }

    private static void printMenu(Menu menu, int depth) {
        String indent = "  ".repeat(depth);
        if (menu.getItems().isEmpty()) {
            System.out.println(indent + "(Empty)");
        }
        for (MenuItem item : menu.getItems()) {
            if (item.getApp() != null) {
                System.out.println(indent + "- [App] " + item.getLabel() + " -> " + item.getApp().getName());
            } else if (item.getSubMenu() != null) {
                System.out.println(indent + "- [SubMenu] " + item.getLabel());
                printMenu(item.getSubMenu(), depth + 1);
            }
        }
    }

    private static void modifyMenu(Scanner scanner) {
        if (currentUser == null || currentUser.getDeviceMenu() == null) {
            System.out.println("User or menu not available.");
            return;
        }
        System.out.println("1. Add application to menu");
        System.out.println("2. Add submenu to menu");
        System.out.println("0. Back");
        String option = scanner.nextLine();
        if ("0".equals(option)) return;

        em.getTransaction().begin();
        if ("1".equals(option)) {
            var apps = em.createQuery("SELECT a FROM App a", App.class).getResultList();
            for (int i = 0; i < apps.size(); i++) {
                System.out.println((i + 1) + ". " + apps.get(i).getName());
            }
            System.out.println("0. Back");
            System.out.print("Select app by number: ");
            try {
                int num = Integer.parseInt(scanner.nextLine());
                if (num == 0) {
                    em.getTransaction().rollback();
                    return;
                } else if (num > 0 && num <= apps.size()) {
                    System.out.print("Enter label for this menu item (0 to cancel): ");
                    String label = scanner.nextLine();
                    if ("0".equals(label)) {
                        em.getTransaction().rollback();
                        return;
                    }
                    MenuItem item = new MenuItem(label, apps.get(num - 1));
                    currentUser.getDeviceMenu().getItems().add(item);
                    em.merge(currentUser);
                    System.out.println("App added to menu.");
                }
            } catch (Exception e) {}
        } else if ("2".equals(option)) {
            System.out.print("Enter submenu name/label (0 to back): ");
            String label = scanner.nextLine();
            if ("0".equals(label)) {
                em.getTransaction().rollback();
                return;
            }
            Menu sub = new Menu(label);
            MenuItem item = new MenuItem(label, sub);
            currentUser.getDeviceMenu().getItems().add(item);
            em.merge(currentUser);
            System.out.println("Submenu added.");
        }
        em.getTransaction().commit();
    }

    private static void startApp(Scanner scanner) {
        if (currentUser == null) return;
        System.out.println("Launching App simulation...");
        System.out.println("User is acting in UI to start an app.");
        System.out.println("(Imagine standard app start logic running here)");
    }

    private static void changeTheme(Scanner scanner) {
        if (currentUser == null) return;
        System.out.print("Enter new theme name (0 to back): ");
        String name = scanner.nextLine();
        if ("0".equals(name) || name.isBlank()) return;

        em.getTransaction().begin();
        Theme t = new Theme(name);
        em.persist(t);
        currentUser.setTheme(t);
        em.merge(currentUser);
        em.getTransaction().commit();

        System.out.println("Theme updated.");
    }

    private static void setBackground(Scanner scanner) {
        if (currentUser == null) return;
        System.out.print("Enter new background image name (0 to back): ");
        String name = scanner.nextLine();
        if ("0".equals(name) || name.isBlank()) return;

        em.getTransaction().begin();
        BackgroundImage b = new BackgroundImage(name);
        em.persist(b);
        currentUser.setBackgroundImage(b);
        em.merge(currentUser);
        em.getTransaction().commit();

        System.out.println("Background updated.");
    }

    private static void manageIcons(Scanner scanner) {
        System.out.println("1. List Icons");
        System.out.println("2. Add Icon");
        System.out.println("0. Back");
        String option = scanner.nextLine();
        if ("0".equals(option)) return;

        if ("1".equals(option)) {
            var icons = em.createQuery("SELECT i FROM Icon i", Icon.class).getResultList();
            icons.forEach(i -> System.out.println(i.getName()));
        } else if ("2".equals(option)) {
            System.out.print("Enter new icon name (0 to back): ");
            String name = scanner.nextLine();
            if ("0".equals(name) || name.isBlank()) return;
            em.getTransaction().begin();
            em.persist(new Icon(name));
            em.getTransaction().commit();
            System.out.println("Icon added.");
        }
    }

    private static void installApp(Scanner scanner) {
        if (currentUser == null) return;
        var apps = em.createQuery("SELECT a FROM App a", App.class).getResultList();
        System.out.println("Available Apps in Store:");
        for (int i = 0; i < apps.size(); i++) {
            System.out.println((i + 1) + ". " + apps.get(i).getName());
        }
        System.out.println("0. Back");
        System.out.print("Select app to install: ");
        try {
            int num = Integer.parseInt(scanner.nextLine());
            if (num == 0) return;
            if (num > 0 && num <= apps.size()) {
                App app = apps.get(num - 1);
                if (currentUser.getInstalledApps().contains(app)) {
                    System.out.println("App already installed.");
                    return;
                }
                em.getTransaction().begin();
                currentUser.getInstalledApps().add(app);
                em.merge(currentUser);
                em.getTransaction().commit();
                System.out.println("App '" + app.getName() + "' installed successfully.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    private static void executeAiCommand(Scanner scanner) {
        if (currentUser == null) {
            System.out.println("Kérlek előbb válassz egy felhasználót!");
            return;
        }
        System.out.print("Miben segíthetek? (pl. indítsd el a Térkép alkalmazást): ");
        String request = scanner.nextLine();
        if (request.isBlank() || request.equals("0")) return;

        StringBuilder apps = new StringBuilder();
        for (App app : currentUser.getInstalledApps()) {
            apps.append(app.getName()).append(", ");
        }
        
        String systemMsg = "Te egy OS szimulátor asszisztense vagy. A felhasználó utasításait le kell fordítanod rendszerparancsokra.\n" +
            "Jelenleg telepített alkalmazások: [" + apps.toString() + "]\n" +
            "Jelenlegi téma: " + (currentUser.getTheme() != null ? currentUser.getTheme().getName() : "Nincs") + "\n" +
            "Válaszolj szigorúan az alábbi JSON formátumban! Csak a JSON-t írd, semmi mást!\n" +
            "Ha egy alkalmazást kell indítani: { \"action\": \"START_APP\", \"target\": \"[Alkalmazás Neve]\" }\n" +
            "Ha témát kell váltani: { \"action\": \"CHANGE_THEME\", \"target\": \"[Új Téma Neve]\" }\n" +
            "Egyéb esetben: { \"action\": \"UNKNOWN\", \"target\": \"\" }";

        System.out.println("Gondolkodom...");
        JsonNode response = aiService.executePrompt(systemMsg, request);

        if (response != null && response.has("action")) {
            String action = response.get("action").asText();
            String target = response.get("target").asText();

            if ("START_APP".equals(action)) {
                System.out.println("\n[AI] Az LLM értelmezte a feladatot: " + target + " alkalmazás indítása.");
                boolean found = false;
                for (App app : currentUser.getInstalledApps()) {
                    if (app.getName().equalsIgnoreCase(target)) {
                        System.out.println("=> A(z) " + target + " alkalmazás elindul... (üzenet a konzolon)");
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.out.println("=> HIBA: A(z) " + target + " nincs telepítve a felhasználónak.");
                }
            } else if ("CHANGE_THEME".equals(action)) {
                System.out.println("\n[AI] Az LLM értelmezte a feladatot: Téma váltás -> " + target);
                em.getTransaction().begin();
                Theme t = new Theme(target);
                em.persist(t);
                currentUser.setTheme(t);
                em.merge(currentUser);
                em.getTransaction().commit();
                System.out.println("=> Téma sikeresen átállítva.");
            } else {
                System.out.println("\n[AI] Nem értem a parancsot.");
            }
        }
    }

    private static void runAiSimulation() {
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
        
        JsonNode response = aiService.executePrompt(systemMsg, "Kérlek generáld le a szimulációs adatokat.");
        
        if (response != null && response.has("users")) {
            em.getTransaction().begin();
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
            em.getTransaction().commit();
            System.out.println("=> Sikeresen generálva és mentve " + userCount + " felhasználó és " + appCount + " alkalmazás!");
        } else {
            System.out.println("Hiba történt a generálás során. Az LLM nem érvényes JSON-t adott vissza.");
        }
    }
}
