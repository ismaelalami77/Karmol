package Login;

import Connection.UserDAO;
import EmployeeView.EmployeeView;
import ManagerView.ManagerView;
import com.example.comp333finalproj.UIHelper;
import com.example.comp333finalproj.UIHelperC;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class Login {
    private BorderPane root;
    private Stage stage;
    private Scene scene;

    private VBox container;

    private Text loginText;
    private TextField userField;
    private PasswordField passField;
    private Button loginButton;

    private ImageView logoView;
    private Image logoImage;

    public Login() {
        root = new BorderPane();


        root.setBackground(new Background(new BackgroundFill(Color.web("#e6f7ef"), CornerRadii.EMPTY, Insets.EMPTY)));

        logoImage = new Image(getClass().getResourceAsStream("/com/example/comp333finalproj/logo.png"));
        logoView = new ImageView(logoImage);
        logoView.setFitWidth(150);
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);


        container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setSpacing(20);
        container.setPadding(new Insets(30, 40, 30, 40));


        loginText = UIHelperC.createTitleText("Login");


        userField = UIHelperC.createStyledTextField("Username");
        passField = UIHelperC.createStyledPassField("Password");


        loginButton = UIHelperC.createStyledButton("Login");


        container.getChildren().addAll(logoView, loginText, userField, passField, loginButton);


        root.setCenter(container);


        stage = new Stage();
        scene = new Scene(root, 400, 500);
        stage.setScene(scene);
        stage.setResizable(false);


        loginButton.setOnAction(e -> LoginHandle());
        userField.setOnAction(e -> passField.requestFocus());
        passField.setOnAction(e -> LoginHandle());


    }

    private void LoginHandle() {
        UserDAO dao = new UserDAO();
        User u = dao.authenticate(userField.getText().trim(), passField.getText());

        if (u == null) {
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Wrong username or password");
            return;
        }

        if ("EMPLOYEE".equalsIgnoreCase(u.getRole())) {
            new EmployeeView(u).show();
            stage.close();
        } else if ("MANAGER".equalsIgnoreCase(u.getRole())) {
            new ManagerView(u).show();
        }
    }


    public void showStage() {
        stage.show();
    }
}
