package hu.agilexpert.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "background_images")
public class BackgroundImage extends BaseEntity {

    @ToString.Include
    private String name;

    public BackgroundImage() {
        super();
    }

    public BackgroundImage(String name) {
        super();
        this.name = name;
    }
}
