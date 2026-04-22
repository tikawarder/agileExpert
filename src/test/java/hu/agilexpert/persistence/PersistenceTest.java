package hu.agilexpert.persistence;

import hu.agilexpert.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceTest {

    private EntityManagerFactory emf;
    private EntityManager em;

    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("agilexpert-test-pu");
        em = emf.createEntityManager();
    }

    @AfterEach
    void tearDown() {
        if (em != null) em.close();
        if (emf != null) emf.close();
    }

    @Test
    void shouldPersistAndLoadUserAccount() {
        // Given
        UserAccount user = new UserAccount("PersistedUser");
        
        // When
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        
        em.clear(); // Clear cache to ensure we load from DB

        // Then
        UserAccount loaded = em.find(UserAccount.class, user.getId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getName()).isEqualTo("PersistedUser");
    }

    @Test
    void shouldPersistUserWithComplexMenuStructure() {
        // Given
        UserAccount user = new UserAccount("ComplexUser");
        Menu rootMenu = new Menu("Root");
        
        Icon icon = new Icon("AppIcon");
        App app = new App("TestApp", icon);
        
        MenuItem item1 = new MenuItem("App Link", app);
        
        Menu subMenu = new Menu("SubMenu");
        MenuItem item2 = new MenuItem("Sub Link", subMenu);
        
        rootMenu.getItems().add(item1);
        rootMenu.getItems().add(item2);
        
        user.setDeviceMenu(rootMenu);

        // When
        em.getTransaction().begin();
        em.persist(icon);
        em.persist(app);
        // MenuItem and Menu are cascaded via UserAccount -> Menu -> MenuItem
        em.persist(user);
        em.getTransaction().commit();

        em.clear();

        // Then
        UserAccount loaded = em.find(UserAccount.class, user.getId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getDeviceMenu()).isNotNull();
        assertThat(loaded.getDeviceMenu().getItems()).hasSize(2);
        
        MenuItem loadedItem1 = loaded.getDeviceMenu().getItems().get(0);
        assertThat(loadedItem1.getApp()).isNotNull();
        assertThat(loadedItem1.getApp().getName()).isEqualTo("TestApp");
        
        MenuItem loadedItem2 = loaded.getDeviceMenu().getItems().get(1);
        assertThat(loadedItem2.getSubMenu()).isNotNull();
        assertThat(loadedItem2.getSubMenu().getName()).isEqualTo("SubMenu");
    }
}
