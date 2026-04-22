package hu.agilexpert.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UserAccountTest {

    @Test
    void shouldCreateUserAccountWithName() {
        // Given
        String userName = "TestUser";

        // When
        UserAccount user = new UserAccount(userName);

        // Then
        assertThat(user.getName()).isEqualTo(userName);
    }

    @Test
    void shouldSetAndGetTheme() {
        // Given
        UserAccount user = new UserAccount("TestUser");
        Theme theme = new Theme("Dark Mode");

        // When
        user.setTheme(theme);

        // Then
        assertThat(user.getTheme()).isEqualTo(theme);
    }

    @Test
    void shouldAddInstalledApp() {
        // Given
        UserAccount user = new UserAccount("TestUser");
        App app = new App("TestApp", new Icon("TestIcon"));

        // When
        user.getInstalledApps().add(app);

        // Then
        assertThat(user.getInstalledApps()).contains(app);
    }

    @Test
    void shouldSetAndGetDeviceMenu() {
        // Given
        UserAccount user = new UserAccount("TestUser");
        Menu menu = new Menu("Main Menu");

        // When
        user.setDeviceMenu(menu);

        // Then
        assertThat(user.getDeviceMenu()).isEqualTo(menu);
    }
}
