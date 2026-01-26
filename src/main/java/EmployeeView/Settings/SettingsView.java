package EmployeeView.Settings;

import Connection.DBUtil;
import Connection.ProductDAO;
import Connection.OrderDAO;
import Connection.EmployeeDAO;

import ManagerView.EmployeeManagement.Employee;
import com.example.comp333finalproj.UIHelperC;
import javafx.application.Platform;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.Connection;

public class SettingsView {
    private BorderPane root;
    private VBox rightVBox, centerVbox;
    private GridPane grid;

    private Employee employee;


    private Label topProductLabel, topClientLabel;
    private Text settingsText;

    private Text employeeFirstNameText, employeeLastNameText,
            employeePhoneText, employeeAddressText, oldPasswordText, newPasswordText, confirmPasswordText;

    private TextField employeeFirstNameTextField, employeeLastNameTextField,
            employeePhoneTextField, employeeAddressTextField;

    private PasswordField currentPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmNewPasswordField;

    private Button updateButton;


    public SettingsView(Employee employee) {
        this.employee = employee;
        root = new BorderPane();

        settingsText = UIHelperC.createTitleText("Settings");

        rightVBox = statsBox();

        centerVbox = new VBox();
        centerVbox.setAlignment(Pos.CENTER);
        centerVbox.setSpacing(20);

        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        employeeFirstNameText = UIHelperC.createInfoText("First Name: ");
        employeeFirstNameTextField = UIHelperC.createStyledTextField("firstName");

        employeeLastNameText = UIHelperC.createInfoText("Last Name: ");
        employeeLastNameTextField = UIHelperC.createStyledTextField("lastName");

        employeePhoneText = UIHelperC.createInfoText("Phone:");
        employeePhoneTextField = UIHelperC.createStyledTextField("Phone");

        employeeAddressText = UIHelperC.createInfoText("Address:");
        employeeAddressTextField = UIHelperC.createStyledTextField("Address");

        oldPasswordText = UIHelperC.createInfoText("Current Password:");
        currentPasswordField = UIHelperC.createStyledPassField("password");

        newPasswordText = UIHelperC.createInfoText("New Password:");
        newPasswordField = UIHelperC.createStyledPassField("password");

        confirmPasswordText = UIHelperC.createInfoText("Confirm Password:");
        confirmNewPasswordField = UIHelperC.createStyledPassField("password");

        grid.add(employeeFirstNameText, 0, 0);
        grid.add(employeeFirstNameTextField, 1, 0);

        grid.add(employeeLastNameText, 0, 1);
        grid.add(employeeLastNameTextField, 1, 1);

        grid.add(employeePhoneText, 0, 2);
        grid.add(employeePhoneTextField, 1, 2);

        grid.add(employeeAddressText, 0, 3);
        grid.add(employeeAddressTextField, 1, 3);

        grid.add(oldPasswordText, 0, 4);
        grid.add(currentPasswordField, 1, 4);

        grid.add(newPasswordText, 0, 5);
        grid.add(newPasswordField, 1, 5);

        grid.add(confirmPasswordText, 0, 6);
        grid.add(confirmNewPasswordField, 1, 6);

        updateButton = UIHelperC.createStyledButton("Update");

        centerVbox.getChildren().addAll(settingsText, grid, updateButton);

        root.setRight(rightVBox);
        root.setCenter(centerVbox);

        refreshAllInBackground();
        fillEmployeeData();

        employeeFirstNameTextField.setOnAction(e -> employeeLastNameTextField.requestFocus());
        employeeLastNameTextField.setOnAction(e -> employeePhoneTextField.requestFocus());
        employeePhoneTextField.setOnAction(e -> employeeAddressTextField.requestFocus());
        employeeAddressTextField.setOnAction(e -> currentPasswordField.requestFocus());
        currentPasswordField.setOnAction(e -> newPasswordField.requestFocus());
        newPasswordField.setOnAction(e -> confirmNewPasswordField.requestFocus());
        confirmNewPasswordField.setOnAction(e -> updateEmployeeAction());

        updateButton.setOnAction(e -> updateEmployeeAction());
    }

    public BorderPane getRoot() {
        return root;
    }

    private void updateEmployeeAction() {

        if (employee == null) {
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Employee data not loaded.");
            return;
        }

        // ---------------------------
        // 1) Update normal fields
        // ---------------------------
        employee.setFirstName(employeeFirstNameTextField.getText().trim());
        employee.setLastName(employeeLastNameTextField.getText().trim());
        employee.setPhoneNumber(employeePhoneTextField.getText().trim());
        employee.setAddress(employeeAddressTextField.getText().trim());

        boolean profileUpdated;
        try (Connection con = DBUtil.getConnection()) {
            profileUpdated = EmployeeDAO.updateEmployee(con, employee);
        } catch (Exception ex) {
            ex.printStackTrace();
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Database error while updating profile.");
            return;
        }

        // ---------------------------
        // 2) Password update (optional)
        // ---------------------------
        String currentPass = currentPasswordField.getText().trim();
        String newPass = newPasswordField.getText().trim();
        String confirmPass = confirmNewPasswordField.getText().trim();

        boolean wantsToChangePassword = !newPass.isEmpty() || !confirmPass.isEmpty() || !currentPass.isEmpty();

        if (wantsToChangePassword) {

            // Must enter the current password
            if (currentPass.isEmpty()) {
                UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Please enter your current password first.");
                return;
            }

            // Must enter new password + confirm
            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Please enter the new password and confirm it.");
                return;
            }

            // Confirm match
            if (!newPass.equals(confirmPass)) {
                UIHelperC.showAlert(Alert.AlertType.INFORMATION, "New password and confirmation do not match.");
                return;
            }

            // Verify old password against DB
            boolean oldCorrect = EmployeeDAO.checkOldPassword(employee.getEmployeeId(), currentPass);
            if (!oldCorrect) {
                UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Current password is incorrect.");
                return;
            }

            // Update password
            boolean passUpdated = EmployeeDAO.updatePassword(employee.getEmployeeId(), newPass);
            if (!passUpdated) {
                UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Password was not updated (DB error).");
                return;
            }

            // Clear password fields after success
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmNewPasswordField.clear();

            UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Profile updated + password changed successfully.");
            return;
        }

        // If only profile updated
        if (profileUpdated) UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Profile updated successfully.");
        UIHelperC.showAlert(Alert.AlertType.INFORMATION, "No changes were saved.");
    }



    public void fillEmployeeData() {
        if (employee == null) {
            return;
        }

        employeeFirstNameTextField.setText(employee.getFirstName());
        employeeLastNameTextField.setText(employee.getLastName());
        employeePhoneTextField.setText(employee.getPhoneNumber());
        employeeAddressTextField.setText(employee.getAddress());
    }

    private VBox statsBox() {
        VBox vBox = new VBox(20);
        vBox.setPadding(new Insets(20));
        vBox.setAlignment(Pos.CENTER);

        topProductLabel = createStatLabel("Top Product", "-");
        topClientLabel = createStatLabel("Top Client", "-");
        vBox.getChildren().addAll(
                topProductLabel, topClientLabel
        );

        return vBox;
    }

    private Label createStatLabel(String title, String value) {
        Label label = new Label(title + "\n" + value);
        label.getStyleClass().add("statistics-square");
        label.setAlignment(Pos.CENTER);
        label.setMinWidth(180);
        return label;
    }

    private void refreshAllInBackground() {
        Thread t = new Thread(() -> {
            try (Connection con = DBUtil.getConnection()) {

                ProductDAO productDAO = new ProductDAO();
                OrderDAO orderDAO = new OrderDAO();


                String topProd = productDAO.getTopProduct();
                String topCli = orderDAO.getTopClient();


                Platform.runLater(() -> {

                    topProductLabel.setText("Top Product\n" + (topProd != null ? topProd : "N/A"));
                    topClientLabel.setText("Top Client\n" + (topCli != null ? topCli : "N/A"));

                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        t.setDaemon(true);
        t.start();
    }
}