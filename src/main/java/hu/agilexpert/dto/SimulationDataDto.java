package hu.agilexpert.dto;

import java.util.List;

public class SimulationDataDto {
    private List<UserDto> users;

    public List<UserDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserDto> users) {
        this.users = users;
    }

    public static class UserDto {
        private String name;
        private String theme;
        private List<AppDto> apps;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTheme() {
            return theme;
        }

        public void setTheme(String theme) {
            this.theme = theme;
        }

        public List<AppDto> getApps() {
            return apps;
        }

        public void setApps(List<AppDto> apps) {
            this.apps = apps;
        }
    }

    public static class AppDto {
        private String name;
        private String icon;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }
    }
}
