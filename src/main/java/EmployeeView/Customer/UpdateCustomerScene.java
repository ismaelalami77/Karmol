package EmployeeView.Customer;

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

public class UpdateCustomerScene {
    private final CustomerView customerView;
    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private GridPane grid;
    private HBox buttonsHbox;
    private VBox centerVbox;

    private Text updateCustomerText, customerNameText,
            customerEmailText, customerPhoneText, customerAddressText;

    private TextField customerNameTextField,
            customerEmailTextFiled, customerPhoneTextFiled, customerAddressTextFiled;

    private Button updateCustomerButton, cancelButton;
    private Customer selectedCustomer;

    public UpdateCustomerScene(CustomerView customerView) {
        this.customerView = customerView;

        root = new BorderPane();

        centerVbox = new VBox();
        centerVbox.setAlignment(Pos.CENTER);
        centerVbox.setSpacing(15);

        updateCustomerText = UIHelperC.createTitleText("Update Customer");

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

        updateCustomerButton = UIHelperC.createStyledButton("Update");
        cancelButton = UIHelperC.createStyledButton("Cancel");
        buttonsHbox.getChildren().addAll(updateCustomerButton, cancelButton);

        centerVbox.getChildren().addAll(updateCustomerText, grid, buttonsHbox);

        root.setCenter(centerVbox);
        stage = new Stage();
        scene = new Scene(root, 650, 500);
        stage.setScene(scene);
        stage.setTitle("Update Customer");

        cancelButton.setOnAction(e -> stage.close());
        updateCustomerButton.setOnAction(e -> updateCustomer());

        customerNameTextField.setOnAction(e -> customerEmailTextFiled.requestFocus());
        customerEmailTextFiled.setOnAction(e -> customerPhoneTextFiled.requestFocus());
        customerPhoneTextFiled.setOnAction(e -> customerAddressTextFiled.requestFocus());
        customerAddressTextFiled.setOnAction(e -> updateCustomer());
    }

    public void setCustomer(Customer customer) {
        this.selectedCustomer = customer;

        customerNameTextField.setText(customer.getCustomerName());
        customerEmailTextFiled.setText(customer.getCustomerEmail());
        customerPhoneTextFiled.setText(customer.getCustomerPhone());
        customerAddressTextFiled.setText(customer.getCustomerAddress());
    }

    private void updateCustomer() {
        if (selectedCustomer == null) return;

        String customerName = customerNameTextField.getText().trim();
        String customerEmail = customerEmailTextFiled.getText().trim();
        String customerPhone = customerPhoneTextFiled.getText().trim();
        String customerAddress = customerAddressTextFiled.getText().trim();

        if (customerName.isEmpty() || customerEmail.isEmpty() || customerPhone.isEmpty() || customerAddress.isEmpty()) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Please fill all fields");
            return;
        }

        if (!isValidName(customerName)) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Customer name is invalid");
            return;
        }

        if (!isValidEmail(customerEmail)) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Customer email is invalid");
            return;
        }

        if (!isValidPhone(customerPhone)) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Phone number is invalid");
            return;
        }

        if (!isValidAddress(customerAddress)) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Customer address is invalid");
            return;
        }

        try (Connection con = DBUtil.getConnection()) {
            CustomerDAO dao = new CustomerDAO();

            if (!customerPhone.equals(selectedCustomer.getCustomerPhone())
                    && dao.phoneExists(con, customerPhone)) {
                UIHelperC.showAlert(Alert.AlertType.WARNING, "This phone number already exists!");
                return;
            }

            boolean updated = dao.updateCustomer(con, selectedCustomer.getCustomerId(),
                    customerName, customerEmail, customerPhone, customerAddress);

            if (updated) {
                selectedCustomer.setCustomerName(customerName);
                selectedCustomer.setCustomerEmail(customerEmail);
                selectedCustomer.setCustomerPhone(customerPhone);
                selectedCustomer.setCustomerAddress(customerAddress);

                CustomerView.refreshTable();
                UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Customer Updated");
                stage.close();
            } else {
                UIHelperC.showAlert(Alert.AlertType.ERROR, "Update failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Database error occurred!");
        }
    }

    public void showStage() {
        stage.show();
    }

    private boolean isValidName(String name) {
        return name.matches("[a-zA-Z]+( [a-zA-Z]+)*");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("\\d{10}");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean isValidAddress(String address) {
        return address.matches("[A-Za-z0-9 ,.]+");
    }
}
