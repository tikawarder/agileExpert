package hu.agilexpert.model;

import jakarta.persistence.*;

@Entity
@Table(name = "menu_items")
public class MenuItem extends BaseEntity {

    private String label;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "app_id")
    private App app;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "submenu_id")
    private Menu subMenu;

    public MenuItem() {
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public Menu getSubMenu() {
        return subMenu;
    }

    public void setSubMenu(Menu subMenu) {
        this.subMenu = subMenu;
    }

    @Override
    public String toString() {
        return "MenuItem{" + "label='" + label + '\'' + '}';
    }
}
