package hu.agilexpert;

import hu.agilexpert.service.DbService;
import hu.agilexpert.ui.ConsoleUi;

public class Main {
    public static void main(String[] args) {
        ConsoleUi ui = new ConsoleUi();
        try {
            ui.start();
        } finally {
            DbService.getInstance().close();
        }
    }
}
