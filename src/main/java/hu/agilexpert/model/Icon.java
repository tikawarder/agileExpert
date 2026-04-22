package hu.agilexpert.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "icons")
public class Icon extends BaseEntity {

    private String name;

    public Icon() {
    }

    public Icon(String name) {
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
        return "Icon{" + "name='" + name + '\'' + '}';
    }
}
