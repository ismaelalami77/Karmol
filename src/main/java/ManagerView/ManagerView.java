package ManagerView;

import Login.Login;
import Login.User;
import ManagerView.EmployeeManagement.ViewEmployees;
import ManagerView.ProductsManagement.ViewProducts;
import Menus.ManagerMenu;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ManagerView {
    private BorderPane root;

    private ViewEmployees viewEmployees;
    private ViewProducts viewProducts;

    ManagerMenu managerMenu = new ManagerMenu(() -> new Login().showStage());
    private Stage stage;
    private Scene scene;

    private VBox mainCenterVbox;

    public ManagerView(User user) {
        root = new BorderPane();

        viewEmployees = new ViewEmployees();
        viewProducts = new ViewProducts();

        root.setBackground(new Background(new BackgroundFill(Color.web("#e6f7ef"), CornerRadii.EMPTY, Insets.EMPTY)));

        mainCenterVbox = new VBox();
        mainCenterVbox.setAlignment(Pos.CENTER);

        root.setTop(managerMenu.getMenuBar());

        stage = new Stage();
        scene = new Scene(root, 900, 600);

        managerMenu.getViewEmployeeMenuItem().setOnAction(e -> {
            root.setCenter(viewEmployees.getRoot());
        });

        managerMenu.getProductsMenuItem().setOnAction(e -> {
            root.setCenter(viewProducts.getRoot());
        });

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("Manager - " + user.getFullName());
    }

    public void show() {
        stage.show();
    }
}
