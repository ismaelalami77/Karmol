package ManagerView.EmployeeManagement;

import Connection.EmployeeDAO;
import Connection.DBUtil;

import EmployeeView.Customer;
import Product.Product;
import com.example.comp333finalproj.UIHelperC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.util.ArrayList;


public class ViewEmployees {
    public static ArrayList<Employee> employees = new ArrayList<>();
    public static ObservableList<Employee> observableEmployees = FXCollections.observableArrayList();
    private BorderPane root;
    private TableView<Employee> employeesTable;
    private VBox leftVBox, centerVBox;
    private Text manageEmployeesText;
    private TextField searchTextField;
    private Button removeBtn, updateBtn, addBtn;
    private AddEmployeeScene addEmployeeScene;
    private UpdateEmployeeScene updateEmployeeScene;

    public ViewEmployees() {
        root = new BorderPane();

        centerVBox = new VBox();
        centerVBox.setAlignment(Pos.CENTER);
        centerVBox.setSpacing(15);
        centerVBox.setPadding(new Insets(20));

        manageEmployeesText = UIHelperC.createTitleText("Manage Employees");

        employeesTable = new TableView<>();
        TableColumn<Employee, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("employeeId"));

        TableColumn<Employee, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Employee, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<Employee, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));


        TableColumn<Employee, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        TableColumn<Employee, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        employeesTable.getColumns().addAll(
                idCol, usernameCol, firstNameCol, lastNameCol, phoneCol, addressCol
        );

        employeesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        employeesTable.setPrefHeight(400);
        employeesTable.setItems(observableEmployees);

        centerVBox.getChildren().addAll(manageEmployeesText, employeesTable);


        leftVBox = new VBox();
        leftVBox.setAlignment(Pos.CENTER);
        leftVBox.setSpacing(15);
        leftVBox.setPadding(new Insets(20));

        searchTextField = UIHelperC.createStyledTextField("Name");

        addBtn = UIHelperC.createStyledButton("Add");
        updateBtn = UIHelperC.createStyledButton("Update");
        removeBtn = UIHelperC.createStyledButton("Remove");

        leftVBox.getChildren().addAll(searchTextField, addBtn, updateBtn, removeBtn);

        root.setCenter(centerVBox);
        root.setLeft(leftVBox);

        addEmployeeScene = new AddEmployeeScene(this);
        updateEmployeeScene = new UpdateEmployeeScene(this);

        addBtn.setOnAction(e -> addEmployeeScene.showStage());
        updateBtn.setOnAction(e -> updateAction());
        removeBtn.setOnAction(e -> deleteEmployee());
        searchTextField.textProperty().addListener((obs, oldV, newV) -> filterTable(newV));

        loadEmployees();
    }

    public static void refreshTable() {
        observableEmployees.clear();
        observableEmployees.addAll(employees);
    }

    private void deleteEmployee() {
        Employee employee = employeesTable.getSelectionModel().getSelectedItem();
        if (employee == null) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Employee");
        confirm.setContentText("Are you sure you want to delete this employee?");

        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try (Connection con = DBUtil.getConnection()) {
                    EmployeeDAO dao = new EmployeeDAO();

                    boolean deleted = dao.deleteEmployeeByID(con, employee.getEmployeeId());

                    if (deleted) {
                        employees.removeIf(c -> c.getEmployeeId() == employee.getEmployeeId());
                        refreshTable();

                        UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Employee deleted successfully");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void updateAction() {
        Employee selectedEmployee = employeesTable.getSelectionModel().getSelectedItem();

        if (selectedEmployee == null) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, " Please select a employee to update");
            return;
        }
        updateEmployeeScene.setEmployee(selectedEmployee);
        updateEmployeeScene.showStage();
    }

    public void loadEmployees() {
        employees.clear();
        observableEmployees.clear();

        employees.addAll(EmployeeDAO.getAllEmployees());
        observableEmployees.addAll(employees);
    }

    public BorderPane getRoot() {
        return root;
    }

    private void filterTable(String text) {
        if (text == null || text.trim().isEmpty()) {
            observableEmployees.setAll(employees);
            return;
        }

        String q = text.trim().toLowerCase();
        ArrayList<Employee> filtered = new ArrayList<>();

        for (Employee e : employees) {
            String n = e.getFullName() == null ? "" : e.getFullName().toLowerCase();


            if (n.contains(q)) {
                filtered.add(e);
            }
        }

        observableEmployees.setAll(filtered);
    }
}
