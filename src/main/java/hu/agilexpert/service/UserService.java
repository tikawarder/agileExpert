package hu.agilexpert.service;

import hu.agilexpert.model.Menu;
import hu.agilexpert.model.UserAccount;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final DbService dbService;

    public UserService() {
        this.dbService = DbService.getInstance();
    }

    public UserAccount createUser(String name) {
        UserAccount newUser = new UserAccount(name);
        Menu menu = new Menu(name + "'s Root Menu");
        newUser.setDeviceMenu(menu);

        dbService.inTransaction(em -> em.persist(newUser));
        logger.info("Created new user: {}", name);
        return newUser;
    }

    public List<UserAccount> getAllUsers() {
        return dbService.inQuery(em ->
            em.createQuery("SELECT u FROM UserAccount u", UserAccount.class).getResultList()
        );
    }

    public UserAccount findUserByName(String name) {
        List<UserAccount> results = dbService.inQuery(em ->
            em.createQuery("SELECT u FROM UserAccount u WHERE u.name = :name", UserAccount.class)
              .setParameter("name", name)
              .getResultList()
        );
        return results.isEmpty() ? null : results.get(0);
    }

    public void saveUser(UserAccount user) {
        dbService.inTransaction(em -> em.merge(user));
    }
}
