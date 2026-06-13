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
@Table(name = "user_accounts")
public class UserAccount extends BaseEntity {

    @ToString.Include
    private String name;

    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "menu_id")
    private Menu deviceMenu;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "background_image_id")
    private BackgroundImage backgroundImage;

    @ToString.Exclude
    @ManyToMany
    @JoinTable(
        name = "user_installed_apps",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "app_id")
    )
    private List<App> installedApps = new ArrayList<>();

    public UserAccount() {
        super();
    }

    public UserAccount(String name) {
        super();
        this.name = name;
    }
}
