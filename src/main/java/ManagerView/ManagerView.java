package ManagerView;

import Login.Login;
import Login.User;
import ManagerView.Dashboard.Dashboard;
import ManagerView.EmployeeManagement.ViewEmployees;
import ManagerView.ProductsManagement.ViewProducts;
import com.example.comp333finalproj.UIHelperC;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ManagerView {

    private BorderPane root;

    private ViewEmployees viewEmployees;
    private ViewProducts viewProducts;
    private Dashboard dashboard;

    private Stage stage;
    private Scene scene;

    private HBox topMenu;

    public ManagerView(User user) {
        root = new BorderPane();

        viewEmployees = new ViewEmployees();
        viewProducts = new ViewProducts();
        dashboard = new Dashboard();

        root.setBackground(new Background(
                new BackgroundFill(Color.web("#e6f7ef"), CornerRadii.EMPTY, Insets.EMPTY)
        ));


        topMenu = new HBox(15);
        topMenu.setPadding(new Insets(15));
        topMenu.setAlignment(Pos.CENTER_LEFT);

        Button employeesBtn = UIHelperC.createMenuButton("Employees");
        Button productsBtn  = UIHelperC.createMenuButton("Products");
        Button dashboardBtn = UIHelperC.createMenuButton("Dashboard");
        Button logoutBtn    = UIHelperC.createMenuButton("Logout");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topMenu.getChildren().addAll(
                employeesBtn,
                productsBtn,
                dashboardBtn,
                spacer,
                logoutBtn
        );


        employeesBtn.setOnAction(e -> root.setCenter(viewEmployees.getRoot()));
        productsBtn.setOnAction(e -> root.setCenter(viewProducts.getRoot()));
        dashboardBtn.setOnAction(e -> root.setCenter(dashboard.getRoot()));

        logoutBtn.setOnAction(e -> {
            stage.close();
            new Login().showStage();
        });

        root.setTop(topMenu);

        root.setCenter(dashboard.getRoot());

        stage = new Stage();
        scene = new Scene(root, 900, 600);

        scene.getStylesheets().add(
                getClass().getResource("/com/example/comp333finalproj/style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("Manager - " + user.getFullName());
    }


    public void show() {
        stage.show();
    }
}
