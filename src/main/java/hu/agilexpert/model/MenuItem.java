package hu.agilexpert.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "menu_items")
public class MenuItem extends BaseEntity {

    @ToString.Include
    private String label;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "app_id")
    private App app;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "submenu_id")
    private Menu subMenu;

    public MenuItem() {
        super();
    }

    public MenuItem(String label) {
        super();
        this.label = label;
    }

    public MenuItem(String label, App app) {
        super();
        this.label = label;
        this.app = app;
    }

    public MenuItem(String label, Menu subMenu) {
        super();
        this.label = label;
        this.subMenu = subMenu;
    }
}
