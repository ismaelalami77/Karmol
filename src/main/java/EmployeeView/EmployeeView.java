package EmployeeView;

import EmployeeView.Cash.CashView;
import EmployeeView.Customer.CustomerView;
import EmployeeView.Orders.OrderHistory;
import Login.Login;
import Login.User;
import com.example.comp333finalproj.UIHelperC;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class EmployeeView {

    private BorderPane root;
    private Stage stage;
    private Scene scene;

    private HBox topMenu;

    private CustomerView customerView;
    private CashView cashView;
    private OrderHistory orderHistory;

    public EmployeeView(User user) {

        root = new BorderPane();


        customerView = new CustomerView();
        cashView = new CashView(user);
        orderHistory = new OrderHistory();


        topMenu = new HBox(15);
        topMenu.setPadding(new Insets(15));
        topMenu.setAlignment(Pos.CENTER_LEFT);

        Button cashBtn = UIHelperC.createMenuButton("Cash");
        Button customerBtn = UIHelperC.createMenuButton("Customers");
        Button ordersBtn = UIHelperC.createMenuButton("Order History");
        Button logoutBtn = UIHelperC.createMenuButton("Logout");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topMenu.getChildren().addAll(
                cashBtn,
                customerBtn,
                ordersBtn,
                spacer,
                logoutBtn
        );


        cashBtn.setOnAction(e -> root.setCenter(cashView.getRoot()));
        customerBtn.setOnAction(e -> root.setCenter(customerView.getRoot()));
        ordersBtn.setOnAction(e -> root.setCenter(orderHistory.getRoot()));

        logoutBtn.setOnAction(e -> {
            stage.close();
            new Login().showStage();
        });


        root.setTop(topMenu);
        root.setCenter(cashView.getRoot());
        root.setBackground(
                new Background(new BackgroundFill(Color.web("#e6f7ef"), CornerRadii.EMPTY, Insets.EMPTY))
        );

        stage = new Stage();
        scene = new Scene(root, 900, 600);

        scene.getStylesheets().add(
                getClass().getResource("/com/example/comp333finalproj/style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("Employee - " + user.getFullName());
        stage.setMaximized(true);
    }



    public void show() {
        stage.show();
    }
}
