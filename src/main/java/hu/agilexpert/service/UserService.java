package hu.agilexpert.service;

import hu.agilexpert.model.Menu;
import hu.agilexpert.model.UserAccount;
import jakarta.persistence.EntityManager;
import java.util.List;

public class UserService {
    private final DbService dbService;

    public UserService() {
        this.dbService = DbService.getInstance();
    }

    public UserAccount createUser(String name) {
        UserAccount newUser = new UserAccount(name);
        Menu menu = new Menu(name + "'s Root Menu");
        newUser.setDeviceMenu(menu);
        
        dbService.inTransaction(em -> em.persist(newUser));
        return newUser;
    }

    public List<UserAccount> getAllUsers() {
        return dbService.getEm()
                .createQuery("SELECT u FROM UserAccount u", UserAccount.class)
                .getResultList();
    }

    public UserAccount findUserByName(String name) {
        List<UserAccount> results = dbService.getEm()
                .createQuery("SELECT u FROM UserAccount u WHERE u.name = :name", UserAccount.class)
                .setParameter("name", name)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public void saveUser(UserAccount user) {
        dbService.inTransaction(em -> em.merge(user));
    }
}
