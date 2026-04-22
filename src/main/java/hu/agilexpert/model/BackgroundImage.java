package hu.agilexpert.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "background_images")
public class BackgroundImage extends BaseEntity {

    private String name;

    public BackgroundImage() {
    }

    public BackgroundImage(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "BackgroundImage{" + "name='" + name + '\'' + '}';
    }
}
