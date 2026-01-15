package EmployeeView;

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
import Connection.DBUtil;
import Connection.CustomerDAO;

import java.sql.Connection;
import java.util.ArrayList;


public class CustomerView {
    public static ArrayList<Customer> customers = new ArrayList<>();
    public static ObservableList<Customer> observableCustomers = FXCollections.observableArrayList();
    private BorderPane root;
    private TableView<Customer> customersTable;
    private VBox leftVBox, centerVBox;
    private Text manageCustomersText;
    private TextField searchTextField;
    private Button removeBtn, updateBtn, addBtn;
    private AddCustomerScene addCustomerScene;
    private UpdateCustomerScene updateCustomerScene;

    public CustomerView() {
        root = new BorderPane();


        centerVBox = new VBox();
        centerVBox.setAlignment(Pos.CENTER);
        centerVBox.setSpacing(15);
        centerVBox.setPadding(new Insets(20));

        manageCustomersText = UIHelperC.createTitleText("Manage Customers");

        customersTable = new TableView<>();
        TableColumn<Customer, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("customerEmail"));
        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
        TableColumn<Customer, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("customerAddress"));

        customersTable.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, addressCol);
        customersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        customersTable.setPrefHeight(400);
        customersTable.setItems(observableCustomers);

        centerVBox.getChildren().addAll(manageCustomersText, customersTable);


        leftVBox = new VBox();
        leftVBox.setAlignment(Pos.CENTER);
        leftVBox.setSpacing(15);
        leftVBox.setPadding(new Insets(20));

        searchTextField = UIHelperC.createStyledTextField("Phone number");

        addBtn = UIHelperC.createStyledButton("Add");
        updateBtn = UIHelperC.createStyledButton("Update");
        removeBtn = UIHelperC.createStyledButton("Remove");

        leftVBox.getChildren().addAll(searchTextField, addBtn, updateBtn, removeBtn);

        root.setCenter(centerVBox);
        root.setLeft(leftVBox);

        loadCustomersFromDB();

        addCustomerScene = new AddCustomerScene(this);
        updateCustomerScene = new UpdateCustomerScene(this);

        addBtn.setOnAction(e -> addCustomerScene.showStage());
        updateBtn.setOnAction(e -> updateAction());
        removeBtn.setOnAction(e -> deleteCustomer());
        searchTextField.textProperty().addListener((obs, oldV, newV) -> filterTable(newV));
    }

    public static void refreshTable() {
        observableCustomers.clear();
        observableCustomers.addAll(customers);
    }

    private void updateAction() {
        Customer selectedCustomer = customersTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, " Please select a customer to update");
            return;
        }
        updateCustomerScene.setCustomer(selectedCustomer);
        updateCustomerScene.showStage();
    }

    private void loadCustomersFromDB() {
        try (Connection con = DBUtil.getConnection()) {
            CustomerDAO dao = new CustomerDAO();
            customers = dao.getAllCustomers(con);
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteCustomer() {
        Customer customer = customersTable.getSelectionModel().getSelectedItem();
        if (customer == null) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Customer");
        confirm.setContentText("Are you sure you want to delete this customer?");

        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try (Connection con = DBUtil.getConnection()) {
                    CustomerDAO dao = new CustomerDAO();

                    boolean deleted = dao.deleteCustomerByID(con, customer.getCustomerId());

                    if (deleted) {
                        customers.removeIf(c -> c.getCustomerId() == customer.getCustomerId());
                        refreshTable();

                        UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Customer deleted successfully");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    public BorderPane getRoot() {
        return root;
    }

    private void filterTable(String text) {
        if (text == null || text.trim().isEmpty()) {
            observableCustomers.setAll(customers);
            return;
        }

        String q = text.trim().toLowerCase();
        ArrayList<Customer> filtered = new ArrayList<>();

        for (Customer c : customers) {
            String n = c.getCustomerPhone();


            if (n.contains(q)) {
                filtered.add(c);
            }
        }

        observableCustomers.setAll(filtered);
    }
}
