package EmployeeView;

import Connection.CustomerDAO;
import Connection.OrderDAO;
import Connection.DBUtil;
import Login.User;
import Product.Product;
import com.example.comp333finalproj.UIHelperC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.util.ArrayList;

public class CashView {
    public static TableView<Product> productsTableView;
    public static ArrayList<Product> products = new ArrayList<>();
    public static ObservableList<Product> productsObservableList = FXCollections.observableArrayList();
    private static Text totalPriceText;
    private BorderPane root;
    private VBox mainCenterVbox, centerVbox, leftVbox, rightVbox;
    private HBox bottomHBox;
    private Button deleteOneBtn, deleteBtn, addBtn, payBtn;
    private TextField customerIDTextField;
    private Text titleText, cashierNameText, customerNameText, orderIDText;
    private AddProductScene addProductScene = new AddProductScene();

    public CashView(User user) {
        root = new BorderPane();

        mainCenterVbox = new VBox();
        mainCenterVbox.setAlignment(Pos.CENTER);

        centerVbox = new VBox();
        centerVbox.setAlignment(Pos.TOP_CENTER);
        centerVbox.setSpacing(15);
        centerVbox.setPadding(new Insets(20));

        leftVbox = new VBox();
        leftVbox.setAlignment(Pos.CENTER);
        leftVbox.setSpacing(15);
        leftVbox.setPadding(new Insets(20));

        rightVbox = new VBox();
        rightVbox.setAlignment(Pos.CENTER);
        rightVbox.setSpacing(15);
        rightVbox.setPadding(new Insets(20));

        bottomHBox = new HBox();
        bottomHBox.setAlignment(Pos.CENTER);
        bottomHBox.setSpacing(15);
        bottomHBox.setPadding(new Insets(15));


        titleText = UIHelperC.createTitleText("Karmol");

        productsTableView = new TableView();

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));

        TableColumn<Product, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("pricePerPiece"));

        TableColumn<Product, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Product, Double> totalPriceCol = new TableColumn<>("Total Price");
        totalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        productsTableView.getColumns().addAll(nameCol, categoryColumn, priceCol, quantityCol, totalPriceCol);
        productsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productsTableView.setPrefHeight(400);
        productsTableView.setItems(productsObservableList);


        centerVbox.getChildren().addAll(titleText, productsTableView);
        mainCenterVbox.getChildren().add(centerVbox);


        addBtn = UIHelperC.createStyledButton("Add Product");
        addBtn.setOnAction(e -> {
            addProductScene.resetToCategories();
            addProductScene.showStage();
        });

        customerIDTextField = UIHelperC.createStyledTextField("Customer phone");
        customerIDTextField.setOnAction(e -> loadCustomerName());


        payBtn = UIHelperC.createStyledButton("Pay");
        payBtn.setOnAction(e -> payAction(user));

        leftVbox.getChildren().addAll(customerIDTextField, addBtn, payBtn);


        customerNameText = UIHelperC.createInfoText("Customer: ");
        cashierNameText = UIHelperC.createInfoText("Cashier: " + user.getUsername());
        orderIDText = UIHelperC.createInfoText("Order #: ");
        double total = calculateTotal();
        totalPriceText = UIHelperC.createInfoText("Total Price: " + total);

        rightVbox.getChildren().addAll(customerNameText, cashierNameText, orderIDText, totalPriceText);

        deleteBtn = UIHelperC.createStyledButton("Delete");
        deleteBtn.setOnAction(e -> deleteAction());
        deleteOneBtn = UIHelperC.createStyledButton("Delete One");
        deleteOneBtn.setOnAction(e -> deleteOneAction());
        bottomHBox.getChildren().addAll(deleteBtn, deleteOneBtn);


        root.setCenter(mainCenterVbox);
        root.setBottom(bottomHBox);
        root.setLeft(leftVbox);
        root.setRight(rightVbox);
    }

    private void payAction(User user) {
        if (products.isEmpty()){
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Receipt is empty");
            return;
        }

        String idText = customerIDTextField.getText().trim();
        if (idText.isEmpty()){
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Enter Customer ID first");
            return;
        }

        int customerID;
        try{
            customerID = Integer.parseInt(idText);
        }catch (NumberFormatException e){
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Invalid Customer ID");
            return;
        }

        int employeeId = user.getId();

        try(Connection con = DBUtil.getConnection()){
            OrderDAO dao = new OrderDAO();
            int newOrderId = dao.createOrderWithItems(con, employeeId, customerID, products);

            products.clear();

            refreshTable();

            customerIDTextField.clear();
            customerNameText.setText("Customer: ");
            orderIDText.setText("Order #: " + newOrderId);

            OrderHistory.loadAllOrdersFromDB();
            UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Payment Successful");
        }catch (Exception e){
            e.printStackTrace();
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Payment failed");
        }
    }

    public static void refreshTable() {
        productsObservableList.clear();
        productsObservableList.addAll(products);

        double total = calculateTotal();
        totalPriceText.setText("Total Price: " + total);
    }

    private static double calculateTotal() {
        double total = 0;

        for (Product product : products) {
            total += product.getTotalPrice();
        }

        return total;
    }

    private void loadCustomerName() {
        String phoneText = customerIDTextField.getText().trim();

        if (phoneText.isEmpty()) {
            customerIDTextField.clear();
            return;
        }

        try {
            int customerPhone = Integer.parseInt(phoneText);

            CustomerDAO customerDAO = new CustomerDAO();
            Customer customer = customerDAO.getCustomerByPhone(customerPhone);

            if (customer != null) {
                customerNameText.setText("Customer: " + customer.getCustomerName());
            } else {
                customerNameText.setText("Customer: ");
            }
        } catch (NumberFormatException e) {
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Invalid Customer ID");
        }
    }

    private void deleteOneAction() {
        Product product = productsTableView.getSelectionModel().getSelectedItem();
        if (product == null) {
            return;
        }

        if (product.getQuantity() > 1){
            product.setQuantity(product.getQuantity() - 1);
        }else {
            products.remove(product);
        }
        refreshTable();
    }

    private void deleteAction(){
        Product product = productsTableView.getSelectionModel().getSelectedItem();
        if (product != null) {
            products.remove(product);
            refreshTable();
        }
    }

    public BorderPane getRoot() {
        return root;
    }
}
