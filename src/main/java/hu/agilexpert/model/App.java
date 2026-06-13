package hu.agilexpert.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "applications")
public class App extends BaseEntity {

    @ToString.Include
    private String name;

    @ToString.Exclude
    @ManyToOne
    private Icon icon;

    public App() {
        super();
    }

    public App(String name, Icon icon) {
        super();
        this.name = name;
        this.icon = icon;
    }
}
