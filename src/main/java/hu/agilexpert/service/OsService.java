package hu.agilexpert.service;

import hu.agilexpert.model.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsService {
    private static final Logger logger = LoggerFactory.getLogger(OsService.class);
    private final DbService dbService;

    public OsService() {
        this.dbService = DbService.getInstance();
    }

    public void setTheme(UserAccount user, String themeName) {
        dbService.inTransaction(em -> {
            Theme t = new Theme(themeName);
            em.persist(t);
            user.setTheme(t);
            em.merge(user);
        });
        logger.info("Theme set to '{}' for user '{}'", themeName, user.getName());
    }

    public void setBackground(UserAccount user, String bgName) {
        dbService.inTransaction(em -> {
            BackgroundImage b = new BackgroundImage(bgName);
            em.persist(b);
            user.setBackgroundImage(b);
            em.merge(user);
        });
        logger.info("Background set to '{}' for user '{}'", bgName, user.getName());
    }

    public List<App> getAllApps() {
        return dbService.inQuery(em ->
            em.createQuery("SELECT a FROM App a", App.class).getResultList()
        );
    }

    public List<Icon> getAllIcons() {
        return dbService.inQuery(em ->
            em.createQuery("SELECT i FROM Icon i", Icon.class).getResultList()
        );
    }

    public void addIcon(String name) {
        dbService.inTransaction(em -> em.persist(new Icon(name)));
    }

    public void installApp(UserAccount user, App app) {
        if (!user.getInstalledApps().contains(app)) {
            dbService.inTransaction(em -> {
                UserAccount managed = em.find(UserAccount.class, user.getId());
                App managedApp = em.find(App.class, app.getId());
                managed.getInstalledApps().add(managedApp);
                user.getInstalledApps().add(app);
            });
            logger.info("App '{}' installed for user '{}'", app.getName(), user.getName());
        }
    }

    public void addAppToMenu(UserAccount user, App app, String label) {
        dbService.inTransaction(em -> {
            App managedApp = em.find(App.class, app.getId());
            MenuItem item = new MenuItem(label, managedApp);
            UserAccount managed = em.find(UserAccount.class, user.getId());
            managed.getDeviceMenu().getItems().add(item);
            user.getDeviceMenu().getItems().add(item);
        });
    }

    public void addSubMenu(UserAccount user, String label) {
        dbService.inTransaction(em -> {
            Menu sub = new Menu(label);
            em.persist(sub);
            MenuItem item = new MenuItem(label, sub);
            UserAccount managed = em.find(UserAccount.class, user.getId());
            managed.getDeviceMenu().getItems().add(item);
            user.getDeviceMenu().getItems().add(item);
        });
    }
}
