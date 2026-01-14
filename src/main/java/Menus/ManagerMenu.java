package Menus;

import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;

public class ManagerMenu {
    private final MenuBar menuBar;
    private final Menu employeeMenu, productsMenu, statMenu, logoutMenu;
    private final MenuItem viewEmployeeMenuItem, viewProductMenuItem,signOutItem;

    public ManagerMenu(Runnable openLoginAction) {
        menuBar = new MenuBar();
        menuBar.getStyleClass().add("employee-menu-bar");

        // Employee menu
        employeeMenu = new Menu("Employee");
        employeeMenu.getStyleClass().add("employee-menu");
        viewEmployeeMenuItem = new MenuItem("View Employee");
        employeeMenu.getItems().add(viewEmployeeMenuItem);

        // Products menu
        productsMenu = new Menu("Products");
        viewProductMenuItem = new MenuItem("View Products");
        productsMenu.getStyleClass().add("employee-menu");
        productsMenu.getItems().add(viewProductMenuItem);
        // add product items here later

        // Statistics menu
        statMenu = new Menu("Statistics");
        statMenu.getStyleClass().add("employee-menu");
        // add stats items here later

        logoutMenu = new Menu("Logout");
        logoutMenu.getStyleClass().add("employee-menu");

        signOutItem = new MenuItem("Sign Out");
        logoutMenu.getItems().add(signOutItem);

        signOutItem.setOnAction(e -> {
            Window w = menuBar.getScene().getWindow();
            if (w instanceof javafx.stage.Stage stage) {
                stage.close();   // closes this window only
            } else {
                w.hide();
            }
            if (openLoginAction != null) openLoginAction.run();
        });


        menuBar.getMenus().addAll(employeeMenu, productsMenu, statMenu, logoutMenu);

        // Optional: if you have a logout item, you can use openLoginAction like this:
        // MenuItem logout = new MenuItem("Logout");
        // logout.setOnAction(e -> { closeAllStages(); openLoginAction.run(); });
        // menuBar.getMenus().add(new Menu("Account", null, logout));
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public Menu getEmployeeMenu() {
        return employeeMenu;
    }

    public MenuItem getViewEmployeeMenuItem() {
        return viewEmployeeMenuItem;
    }

    public Menu getProductsMenuItem() {
        return productsMenu;
    }
}
