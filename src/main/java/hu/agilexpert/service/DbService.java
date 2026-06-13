package hu.agilexpert.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbService {
    private static final Logger logger = LoggerFactory.getLogger(DbService.class);
    private static DbService instance;
    private final EntityManagerFactory emf;

    private DbService() {
        this.emf = Persistence.createEntityManagerFactory("agilexpert-pu");
    }

    public static synchronized DbService getInstance() {
        if (instance == null) {
            instance = new DbService();
        }
        return instance;
    }

    public void inTransaction(Consumer<EntityManager> action) {
        EntityManager em = emf.createEntityManager();
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
        } finally {
            em.close();
        }
    }

    public <T> T inQuery(Function<EntityManager, T> query) {
        EntityManager em = emf.createEntityManager();
        try {
            return query.apply(em);
        } finally {
            em.close();
        }
    }

    public void close() {
        if (emf.isOpen()) emf.close();
    }
}
