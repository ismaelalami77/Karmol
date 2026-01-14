package ManagerView.EmployeeManagement;

import Connection.DBUtil;
import Connection.EmployeeDAO;
import EmployeeView.CustomerView;
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

import java.sql.Connection;

public class AddEmployeeScene {
    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private GridPane grid;
    private HBox buttonsHbox;
    private VBox centerVbox;

    private Text addEmployeeText, employeeUsernameText, employeeFirstNameText, employeeLastNameText,
            employeePhoneText, employeeAddressText, employeePasswordText ;

    private TextField employeeUsernameTextField, employeeFirstNameTextField, employeeLastNameTextField,
            employeePhoneTextField, employeeAddressTextField;

    private PasswordField employeePasswordField;


    private Button addEmployeeButton, cancelButton;

    private final ViewEmployees viewEmployees;

    public AddEmployeeScene(ViewEmployees viewEmployees) {
        root = new BorderPane();

        this.viewEmployees = viewEmployees;

        centerVbox = new VBox();
        centerVbox.setAlignment(Pos.CENTER);
        centerVbox.setSpacing(15);

        addEmployeeText = UIHelperC.createTitleText("Add Employee");

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

        addEmployeeButton = UIHelperC.createStyledButton("Add");
        cancelButton = UIHelperC.createStyledButton("Cancel");
        buttonsHbox.getChildren().addAll(addEmployeeButton, cancelButton);

        centerVbox.getChildren().addAll(addEmployeeText, grid, buttonsHbox);

        root.setCenter(centerVbox);
        stage = new Stage();
        scene = new Scene(root, 650, 500);
        stage.setScene(scene);
        stage.setTitle("Add Employee");

        cancelButton.setOnAction(e -> stage.close());
        addEmployeeButton.setOnAction(e -> addEmployee());

        employeeUsernameTextField.setOnAction(e -> employeeFirstNameTextField.requestFocus());
        employeeFirstNameTextField.setOnAction(e -> employeeLastNameTextField.requestFocus());
        employeeLastNameTextField.setOnAction(e -> employeePhoneTextField.requestFocus());
        employeePhoneTextField.setOnAction(e -> employeeAddressTextField.requestFocus());
        employeeAddressTextField.setOnAction(e -> employeePasswordField.requestFocus());
    }

    private void addEmployee() {

        String username  = employeeUsernameTextField.getText().trim();
        String firstName = employeeFirstNameTextField.getText().trim();
        String lastName  = employeeLastNameTextField.getText().trim();
        String phone     = employeePhoneTextField.getText().trim();
        String address   = employeeAddressTextField.getText().trim();
        String password  = employeePasswordField.getText().trim();

        // Validation (same style)
        if (firstName.isEmpty()) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Please enter first name!");
            return;
        }
        if (lastName.isEmpty()) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Please enter last name!");
            return;
        }
        if (username.isEmpty()) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Please enter username!");
            return;
        }
        if (password.isEmpty()) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Please enter password!");
            return;
        }
        if (phone.isEmpty()) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Please enter phone number!");
            return;
        }
        if (address.isEmpty()) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Please enter address!");
            return;
        }

        Employee newEmployee = new Employee(
                0,
                firstName,
                lastName,
                username,
                password,
                phone,
                address
        );

        try (Connection con = DBUtil.getConnection()) {

            // insert and get generated id
            int newID = EmployeeDAO.insertEmployee(con, newEmployee);

            if (newID != -1) {

                // add to table list (like you did in customers)
                viewEmployees.employees.add(new Employee(
                        newID,
                        newEmployee.getFirstName(),
                        newEmployee.getLastName(),
                        newEmployee.getUsername(),
                        newEmployee.getPassword(),
                        newEmployee.getPhoneNumber(),
                        newEmployee.getAddress()
                ));

                viewEmployees.refreshTable();

                UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Employee added!");
                stage.close();

            } else {
                UIHelperC.showAlert(Alert.AlertType.ERROR, "Employee could not be inserted!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Database error while adding employee!");
        }
    }


    public void showStage() {
        stage.show();
    }
}
