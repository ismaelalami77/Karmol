package ManagerView.EmployeeManagement;

import com.example.comp333finalproj.UIHelperC;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import Connection.DBUtil;
import Connection.EmployeeDAO;

import java.sql.Connection;

public class UpdateEmployeeScene {
    private final ViewEmployees viewEmployees;
    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private GridPane grid;
    private HBox buttonsHbox;
    private VBox centerVbox;

    private Text updateEmployeeText, employeeUsernameText, employeeFirstNameText, employeeLastNameText,
            employeePhoneText, employeeAddressText, employeePasswordText ;

    private TextField employeeUsernameTextField, employeeFirstNameTextField, employeeLastNameTextField,
            employeePhoneTextField, employeeAddressTextField;

    private PasswordField employeePasswordField;

    private Button updateEmployeeButton, cancelButton;
    private Employee selectedEmployee;

    public UpdateEmployeeScene(ViewEmployees viewEmployees) {
        this.viewEmployees = viewEmployees;

        root = new BorderPane();

        centerVbox = new VBox();
        centerVbox.setAlignment(Pos.CENTER);
        centerVbox.setSpacing(15);

        updateEmployeeText = UIHelperC.createTitleText("Update Employee");

        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        employeeUsernameText = UIHelperC.createInfoText("Username: ");
        employeeUsernameTextField = UIHelperC.createStyledTextField("username");

        employeeFirstNameText = UIHelperC.createInfoText("First Name: ");
        employeeFirstNameTextField = UIHelperC.createStyledTextField("firstName");

        employeeLastNameText = UIHelperC.createInfoText("Last Name: ");
        employeeLastNameTextField = UIHelperC.createStyledTextField("lastName");

        employeePhoneText = UIHelperC.createInfoText("Phone:");
        employeePhoneTextField = UIHelperC.createStyledTextField("Phone");

        employeeAddressText = UIHelperC.createInfoText("Address:");
        employeeAddressTextField = UIHelperC.createStyledTextField("Address");

        employeePasswordText = UIHelperC.createInfoText("Password:");
        employeePasswordField = UIHelperC.createStyledPassField("password");

        grid.add(employeeUsernameText, 0, 0);
        grid.add(employeeUsernameTextField, 1, 0);

        grid.add(employeeFirstNameText, 0, 1);
        grid.add(employeeFirstNameTextField, 1, 1);

        grid.add(employeeLastNameText, 0, 2);
        grid.add(employeeLastNameTextField, 1, 2);

        grid.add(employeePhoneText, 0, 3);
        grid.add(employeePhoneTextField, 1, 3);

        grid.add(employeeAddressText, 0, 4);
        grid.add(employeeAddressTextField, 1, 4);

        grid.add(employeePasswordText, 0, 5);
        grid.add(employeePasswordField, 1, 5);

        buttonsHbox = new HBox();
        buttonsHbox.setAlignment(Pos.CENTER);
        buttonsHbox.setSpacing(15);
        buttonsHbox.setPadding(new Insets(20));

        updateEmployeeButton = UIHelperC.createStyledButton("Update");
        cancelButton = UIHelperC.createStyledButton("Cancel");
        buttonsHbox.getChildren().addAll(updateEmployeeButton, cancelButton);

        centerVbox.getChildren().addAll(updateEmployeeText, grid, buttonsHbox);

        root.setCenter(centerVbox);
        stage = new Stage();
        scene = new Scene(root, 650, 500);
        stage.setScene(scene);
        stage.setTitle("Update Employee");


        cancelButton.setOnAction(e -> stage.close());
        updateEmployeeButton.setOnAction(e -> updateEmployee());

        employeeUsernameTextField.setOnAction(e -> employeeFirstNameTextField.requestFocus());
        employeeFirstNameTextField.setOnAction(e -> employeeLastNameTextField.requestFocus());
        employeeLastNameTextField.setOnAction(e -> employeePhoneTextField.requestFocus());
        employeePhoneTextField.setOnAction(e -> employeeAddressTextField.requestFocus());
        employeeAddressTextField.setOnAction(e -> employeePasswordField.requestFocus());
    }

    public void setEmployee(Employee employee) {
        this.selectedEmployee = employee;

        employeeUsernameTextField.setText(employee.getUsername());
        employeeFirstNameTextField.setText(employee.getFirstName());
        employeeLastNameTextField.setText(employee.getLastName());
        employeePhoneTextField.setText(employee.getPhoneNumber());
        employeeAddressTextField.setText(employee.getAddress());
        employeePasswordField.setText(employee.getPassword());
    }

    private void updateEmployee() {

        if (selectedEmployee == null) {
            return;
        }

        String employeeUsername  = employeeUsernameTextField.getText().trim();
        String employeeFirstName = employeeFirstNameTextField.getText().trim();
        String employeeLastName  = employeeLastNameTextField.getText().trim();
        String employeePhone     = employeePhoneTextField.getText().trim();
        String employeeAddress   = employeeAddressTextField.getText().trim();
        String employeePassword  = employeePasswordField.getText().trim();

        if (employeeUsername.isEmpty() || employeeFirstName.isEmpty() ||
                employeeLastName.isEmpty() || employeePhone.isEmpty() ||
                employeeAddress.isEmpty() || employeePassword.isEmpty()) {

            UIHelperC.showAlert(Alert.AlertType.WARNING, "Please fill all fields");
            return;
        }

        if (!isValidName(employeeUsername) || !isValidName(employeeFirstName) || !isValidName(employeeLastName)) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Please enter a valid name");
            return;
        }

        if (!isValidPhone(employeePhone)) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Please enter a valid phone number");
            return;
        }

        if (!isValidAddress(employeeAddress)) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Please enter a valid address");
            return;
        }


        selectedEmployee.setUsername(employeeUsername);
        selectedEmployee.setFirstName(employeeFirstName);
        selectedEmployee.setLastName(employeeLastName);
        selectedEmployee.setPhoneNumber(employeePhone);
        selectedEmployee.setAddress(employeeAddress);
        selectedEmployee.setPassword(employeePassword);

        try (Connection con = DBUtil.getConnection()) {

            boolean updated = EmployeeDAO.updateEmployee(con, selectedEmployee);

            if (updated) {
                ViewEmployees.refreshTable();
                UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Employee updated successfully");
                stage.close();
            } else {
                UIHelperC.showAlert(Alert.AlertType.ERROR, "Update failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void showStage() {
        stage.show();
    }

    private boolean isValidName(String name) {
        return name.matches("[a-zA-Z]+");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("\\d{10}");
    }

    private boolean isValidAddress(String address) {
        return address.matches("[A-Za-z0-9 ,.]+");
    }
}
