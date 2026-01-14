package Menus;

import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Window;

public class EmployeeMenu {

    private MenuItem cashMenuItem, customerMenuItem, orderHistoryMenuItem, signOutItem;
    private MenuBar menuBar;
    private Menu cashMenu, manageCustomerMenu, orderHistoryMenu, logoutMenu;

    public EmployeeMenu(Runnable openLoginAction) {
        menuBar = new MenuBar();
        menuBar.getStyleClass().add("employee-menu-bar");


        cashMenu = new Menu("Cash");
        cashMenu.getStyleClass().add("employee-menu");
        cashMenuItem = new MenuItem("Cash");
        cashMenu.getItems().add(cashMenuItem);
        cashMenuItem.setOnAction(event -> {

        });


        manageCustomerMenu = new Menu("Customers");
        manageCustomerMenu.getStyleClass().add("employee-menu");
        customerMenuItem = new MenuItem("Customers");
        manageCustomerMenu.getItems().add(customerMenuItem);


        orderHistoryMenu = new Menu("Order History");
        orderHistoryMenu.getStyleClass().add("employee-menu");
        orderHistoryMenuItem = new MenuItem("Order History");
        orderHistoryMenu.getItems().add(orderHistoryMenuItem);



        logoutMenu = new Menu("Logout");
        logoutMenu.getStyleClass().add("employee-menu");

        signOutItem = new MenuItem("Sign Out");
        logoutMenu.getItems().add(signOutItem);

        signOutItem.setOnAction(e -> {
            closeAllStages();
            if (openLoginAction != null) openLoginAction.run();
        });

        menuBar.getMenus().addAll(
                cashMenu,
                manageCustomerMenu,
                orderHistoryMenu,
                logoutMenu
        );
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    private void closeAllStages() {
        var windows = new java.util.ArrayList<>(Window.getWindows());
        for (Window w : windows) {
            if (w != null && w.isShowing()) {
                w.hide();
            }
        }
    }

    public MenuItem getCashMenuItem() {
        return cashMenuItem;
    }

    public MenuItem getCustomerMenuItem() {
        return customerMenuItem;
    }

    public MenuItem getOrderHistoryMenuItem() {
        return orderHistoryMenuItem;
    }

}
