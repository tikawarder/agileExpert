package hu.agilexpert.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MenuItemTest {

    @Test
    void shouldCreateAppMenuItem() {
        // Given
        App app = new App("Browser", null);
        String label = "Internet";

        // When
        MenuItem item = new MenuItem(label, app);

        // Then
        assertThat(item.getLabel()).isEqualTo(label);
        assertThat(item.getApp()).isEqualTo(app);
        assertThat(item.getSubMenu()).isNull();
    }

    @Test
    void shouldCreateSubMenuMenuItem() {
        // Given
        Menu subMenu = new Menu("System Settings");
        String label = "Settings";

        // When
        MenuItem item = new MenuItem(label, subMenu);

        // Then
        assertThat(item.getLabel()).isEqualTo(label);
        assertThat(item.getSubMenu()).isEqualTo(subMenu);
        assertThat(item.getApp()).isNull();
    }
}
