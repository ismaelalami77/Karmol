package EmployeeView;

import Connection.DBUtil;
import Connection.OrderDAO;
import com.example.comp333finalproj.UIHelperC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Objects;

public class OrderHistory {
    public static TableView<Order> ordersHistoryTableView;
    public static ArrayList<Order> ordersArrayList = new ArrayList<>();
    public static ObservableList<Order> ordersObservableList = FXCollections.observableArrayList();

    private BorderPane root;
    private VBox centerVbox, leftVbox;
    private TextField customerIDTextField;
    private Text titleText;

    public OrderHistory() {
        root = new BorderPane();

        centerVbox = new VBox();
        centerVbox.setAlignment(Pos.CENTER);
        centerVbox.setSpacing(15);
        centerVbox.setPadding(new Insets(20));

        leftVbox = new VBox();
        leftVbox.setAlignment(Pos.CENTER);
        leftVbox.setSpacing(15);
        leftVbox.setPadding(new Insets(20));


        titleText = UIHelperC.createTitleText("Order History");

        ordersHistoryTableView = new TableView<>();

        TableColumn<Order, Integer> orderIDCol = new TableColumn("Order ID");


        TableColumn<Order, Integer> employeeIDCol = new TableColumn("Employee ID");


        TableColumn<Order, Integer> customerIDCol = new TableColumn("Customer ID");


        TableColumn<Order, Object> orderDateCol = new TableColumn("Order Date");


        TableColumn<Order, Integer> amountCol = new TableColumn("Amount");

        orderIDCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        employeeIDCol.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        customerIDCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn printCol = new TableColumn("Print");

        ordersHistoryTableView.getColumns().addAll(orderIDCol, employeeIDCol, customerIDCol,
                orderDateCol, amountCol, printCol);
        ordersHistoryTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ordersHistoryTableView.setPrefHeight(400);
        ordersHistoryTableView.setItems(ordersObservableList);

        centerVbox.getChildren().addAll(titleText, ordersHistoryTableView);

        customerIDTextField = UIHelperC.createStyledTextField("Customer ID");
        customerIDTextField.setOnAction(e -> searchByCustomer());

        leftVbox.getChildren().addAll(customerIDTextField);

        root.setCenter(centerVbox);
        root.setLeft(leftVbox);

        loadAllOrdersFromDB();
    }

    public static void loadAllOrdersFromDB() {
        try(Connection con = DBUtil.getConnection()) {
            OrderDAO dao = new OrderDAO();
            ordersArrayList = dao.getAllOrders(con);
            refreshTable();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void searchByCustomer() {
        String txt = customerIDTextField.getText().trim();

        if (txt.isEmpty()) {
            loadAllOrdersFromDB();
            return;
        }

        try{
            int customerID = Integer.parseInt(txt);
            try(Connection con = DBUtil.getConnection()){
                OrderDAO dao = new OrderDAO();
                ordersArrayList = dao.getOrdersByCustomerId(con, customerID);
                refreshTable();
            }
        }catch (Exception e){
            UIHelperC.showAlert(Alert.AlertType.WARNING, "Invalid Customer ID");
        }
    }

    public static void refreshTable() {
        ordersObservableList.clear();
        ordersObservableList.addAll(ordersArrayList);
    }

    public BorderPane getRoot() {
        return root;
    }
}

/*
     table:
    orderID, employeeId, customerID, orderDate, amount, print
    search bar for order by customer id
 */
