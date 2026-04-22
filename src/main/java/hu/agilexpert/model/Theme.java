package hu.agilexpert.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "themes")
public class Theme extends BaseEntity {

    private String name;

    public Theme() {
    }

    public Theme(String name) {
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
        return "Theme{" + "name='" + name + '\'' + '}';
    }
}
