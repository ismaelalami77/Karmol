package EmployeeView;

import com.example.comp333finalproj.UIHelper;
import com.example.comp333finalproj.UIHelperC;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import Connection.DBUtil;
import Connection.CustomerDAO;

import java.sql.Connection;

public class AddCustomerScene {
    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private GridPane grid;
    private HBox buttonsHbox;
    private VBox centerVbox;

    private Text addCustomerText, customerNameText,
            customerEmailText, customerPhoneText, customerAddressText;
    private TextField customerNameTextField,
            customerEmailTextFiled, customerPhoneTextFiled, customerAddressTextFiled;
    private Button addCustomerButton, cancelButton;

    private final CustomerView customerView;

    public AddCustomerScene(CustomerView customerView) {
        this.customerView = customerView;

        root = new BorderPane();

        centerVbox = new VBox();
        centerVbox.setAlignment(Pos.CENTER);
        centerVbox.setSpacing(15);

        addCustomerText = UIHelperC.createTitleText("Add Customer");

        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        customerNameText = UIHelperC.createInfoText("Name:");
        customerNameTextField = UIHelperC.createStyledTextField("Name");
        customerEmailText = UIHelperC.createInfoText("Email:");
        customerEmailTextFiled = UIHelperC.createStyledTextField("Email");
        customerPhoneText = UIHelperC.createInfoText("Phone:");
        customerPhoneTextFiled = UIHelperC.createStyledTextField("Phone");
        customerAddressText = UIHelperC.createInfoText("Address:");
        customerAddressTextFiled = UIHelperC.createStyledTextField("Address");

        grid.add(customerNameText, 0, 0);
        grid.add(customerNameTextField, 1, 0);

        grid.add(customerEmailText, 0, 1);
        grid.add(customerEmailTextFiled, 1, 1);

        grid.add(customerPhoneText, 0, 2);
        grid.add(customerPhoneTextFiled, 1, 2);

        grid.add(customerAddressText, 0, 3);
        grid.add(customerAddressTextFiled, 1, 3);


        buttonsHbox = new HBox();
        buttonsHbox.setAlignment(Pos.CENTER);
        buttonsHbox.setSpacing(15);
        buttonsHbox.setPadding(new Insets(20));

        addCustomerButton = UIHelperC.createStyledButton("Add");
        cancelButton = UIHelperC.createStyledButton("Cancel");
        buttonsHbox.getChildren().addAll(addCustomerButton, cancelButton);

        centerVbox.getChildren().addAll(addCustomerText, grid, buttonsHbox);

        root.setCenter(centerVbox);
        stage = new Stage();
        scene = new Scene(root, 650, 500);
        stage.setScene(scene);
        stage.setTitle("Add Customer");


        cancelButton.setOnAction(e -> stage.close());
        addCustomerButton.setOnAction(e -> addCustomer());

        customerNameTextField.setOnAction(e -> customerEmailTextFiled.requestFocus());
        customerEmailTextFiled.setOnAction(e -> customerPhoneTextFiled.requestFocus());
        customerPhoneTextFiled.setOnAction(e -> customerAddressTextFiled.requestFocus());
    }

    private void addCustomer() {
        String name = customerNameTextField.getText().trim();
        String email = customerEmailTextFiled.getText().trim();
        String phone = customerPhoneTextFiled.getText().trim();
        String address = customerAddressTextFiled.getText().trim();
        if (name.isEmpty()) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Please enter a name!");
            return;
        }
        if (email.isEmpty()) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Please enter an email!");
            return;
        }
        if (phone.isEmpty()) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Please enter a phone number!");
            return;
        }
        if (address.isEmpty()) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Please enter an address!");
            return;
        }

        Customer newCustomer = new Customer(0, name, email, phone, address);

        try(Connection con = DBUtil.getConnection()){
            CustomerDAO dao = new CustomerDAO();
            int newID = dao.insertCustomer(con, newCustomer);

            if (newID != -1){
                CustomerView.customers.add(new Customer(
                        newID,
                        newCustomer.getCustomerName(),
                        newCustomer.getCustomerEmail(),
                        newCustomer.getCustomerPhone(),
                        newCustomer.getCustomerAddress()
                ));
                CustomerView.refreshTable();
                UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Customer added!");
                stage.close();
            }else {
                UIHelperC.showAlert(Alert.AlertType.ERROR, "Customer could not be inserted!");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void showStage() {
        stage.show();
    }
}
