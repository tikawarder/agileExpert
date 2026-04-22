package hu.agilexpert.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {

    // Subclass for testing
    static class TestEntity extends BaseEntity {
        TestEntity() { super(); }
        TestEntity(String id) { super(id); }
    }

    @Test
    void shouldGenerateIdOnCreation() {
        // When
        TestEntity entity = new TestEntity();

        // Then
        assertThat(entity.getId()).isNotNull().isNotBlank();
        assertThat(entity.getId()).hasSize(36); // UUID length
    }

    @Test
    void shouldAcceptCustomId() {
        // Given
        String customId = "custom-id-123";

        // When
        TestEntity entity = new TestEntity(customId);

        // Then
        assertThat(entity.getId()).isEqualTo(customId);
    }
}
