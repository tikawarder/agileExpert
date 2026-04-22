package hu.agilexpert.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MenuTest {

    @Test
    void shouldCreateMenuWithName() {
        // Given
        String menuName = "Main Menu";

        // When
        Menu menu = new Menu(menuName);

        // Then
        assertThat(menu.getName()).isEqualTo(menuName);
        assertThat(menu.getItems()).isEmpty();
    }

    @Test
    void shouldAddMenuItem() {
        // Given
        Menu menu = new Menu("Main Menu");
        MenuItem item = new MenuItem("Open App", new App("TestApp", null));

        // When
        menu.getItems().add(item);

        // Then
        assertThat(menu.getItems()).hasSize(1);
        assertThat(menu.getItems().get(0)).isEqualTo(item);
    }

    @Test
    void shouldAddSubMenu() {
        // Given
        Menu menu = new Menu("Main Menu");
        Menu subMenu = new Menu("Settings");
        MenuItem item = new MenuItem("Settings", subMenu);

        // When
        menu.getItems().add(item);

        // Then
        assertThat(menu.getItems()).hasSize(1);
        assertThat(menu.getItems().get(0).getSubMenu()).isEqualTo(subMenu);
    }
}
