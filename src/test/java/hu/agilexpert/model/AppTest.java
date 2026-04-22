package hu.agilexpert.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AppTest {

    @Test
    void shouldCreateAppWithNameAndIcon() {
        // Given
        String appName = "Browser";
        Icon icon = new Icon("Globe");

        // When
        App app = new App(appName, icon);

        // Then
        assertThat(app.getName()).isEqualTo(appName);
        assertThat(app.getIcon()).isEqualTo(icon);
    }

    @Test
    void shouldUpdateAppName() {
        // Given
        App app = new App("Old Name", new Icon("Icon"));

        // When
        app.setName("New Name");

        // Then
        assertThat(app.getName()).isEqualTo("New Name");
    }
}
