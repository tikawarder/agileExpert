package hu.agilexpert.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbService {
    private static final Logger logger = LoggerFactory.getLogger(DbService.class);
    private static DbService instance;
    private final EntityManagerFactory emf;
    private final EntityManager em;

    private DbService() {
        this.emf = Persistence.createEntityManagerFactory("agilexpert-pu");
        this.em = emf.createEntityManager();
    }

    public static synchronized DbService getInstance() {
        if (instance == null) {
            instance = new DbService();
        }
        return instance;
    }

    public EntityManager getEm() {
        return em;
    }

    public void inTransaction(Consumer<EntityManager> action) {
        try {
            em.getTransaction().begin();
            action.accept(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Transaction error: {}", e.getMessage(), e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void close() {
        if (em.isOpen()) em.close();
        if (emf.isOpen()) emf.close();
    }
}
