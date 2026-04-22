package hu.agilexpert.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "applications")
public class App extends BaseEntity {

    private String name;

    @ManyToOne
    private Icon icon;

    public App() {
    }

    public App(String name, Icon icon) {
        super();
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "App{" + "name='" + name + '\'' + '}';
    }
}
