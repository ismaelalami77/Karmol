package EmployeeView;

import Login.Login;
import Login.User;
import Menus.EmployeeMenu;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class EmployeeView {
    private BorderPane root;

    private Stage stage;
    private Scene scene;

    private VBox mainCenterVbox;

    EmployeeMenu employeeMenu = new EmployeeMenu(() -> new Login().showStage());

    CustomerView customerView;
    CashView cashView;
    OrderHistory orderHistory;



    public EmployeeView(User user) {
        root = new BorderPane();

        mainCenterVbox = new VBox();
        mainCenterVbox.setAlignment(Pos.CENTER);

        customerView = new CustomerView();
        cashView = new CashView(user);
        orderHistory = new OrderHistory();

        root.setBackground(new Background(new BackgroundFill(Color.web("#e6f7ef"), CornerRadii.EMPTY, Insets.EMPTY)));

        stage = new Stage();
        scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(
                getClass().getResource("/com/example/comp333finalproj/employee-menu.css").toExternalForm()
        );
        scene.getStylesheets().add(
                getClass().getResource("/com/example/comp333finalproj/style.css").toExternalForm()
        );

        root.setTop(employeeMenu.getMenuBar());
        root.setCenter(cashView.getRoot());

        employeeMenu.getCashMenuItem().setOnAction(e -> {
            root.setCenter(cashView.getRoot());
        });
        employeeMenu.getCustomerMenuItem().setOnAction(e -> {
            root.setCenter(customerView.getRoot());
        });

        employeeMenu.getOrderHistoryMenuItem().setOnAction(e -> {
            root.setCenter(orderHistory.getRoot());
        });

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("Employee - " + user.getFullName());
    }

    public void show() {
        stage.show();
    }
}
