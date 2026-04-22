package hu.agilexpert.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_accounts")
public class UserAccount extends BaseEntity {

    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "menu_id")
    private Menu deviceMenu;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @ManyToOne
    @JoinColumn(name = "background_image_id")
    private BackgroundImage backgroundImage;

    @ManyToMany
    @JoinTable(
        name = "user_installed_apps",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "app_id")
    )
    private java.util.List<App> installedApps = new java.util.ArrayList<>();

    public UserAccount() {
    }

    public UserAccount(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Menu getDeviceMenu() {
        return deviceMenu;
    }

    public void setDeviceMenu(Menu deviceMenu) {
        this.deviceMenu = deviceMenu;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public BackgroundImage getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(BackgroundImage backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public java.util.List<App> getInstalledApps() {
        return installedApps;
    }

    public void setInstalledApps(java.util.List<App> installedApps) {
        this.installedApps = installedApps;
    }

    @Override
    public String toString() {
        return "UserAccount{" + "name='" + name + '\'' + '}';
    }
}
