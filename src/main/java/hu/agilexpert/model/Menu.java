package hu.agilexpert.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "menus")
public class Menu extends BaseEntity {

    @ToString.Include
    private String name;

    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "menu_id")
    private List<MenuItem> items = new ArrayList<>();

    public Menu() {
        super();
    }

    public Menu(String name) {
        super();
        this.name = name;
    }
}
