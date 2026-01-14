package ManagerView.ProductsManagement;

import Connection.DBUtil;
import Connection.ProductDAO;
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

public class ViewProducts {
    public static TableView<Product> productsTableView;
    public static ArrayList<Product> products = new ArrayList<>();
    public static ObservableList<Product> productsObservableList = FXCollections.observableArrayList();

    private BorderPane root;
    private VBox leftVBox, centerVBox;
    private Text manageProductsText;
    private TextField searchTextField;

    private Button editBtn, addBtn;

    private AddShipment addShipment;

    public ViewProducts() {
        root = new BorderPane();

        // ✅ pass refresh callback to AddShipment (see note below)
        addShipment = new AddShipment(this::refreshTable);

        centerVBox = new VBox();
        centerVBox.setAlignment(Pos.CENTER);
        centerVBox.setSpacing(15);
        centerVBox.setPadding(new Insets(20));

        manageProductsText = UIHelperC.createTitleText("Manage Products");

        productsTableView = new TableView<>();

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));

        TableColumn<Product, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<Product, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("pricePerPiece"));

        productsTableView.getColumns().addAll(nameCol, categoryColumn, quantityCol, priceCol);
        productsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productsTableView.setPrefHeight(400);
        productsTableView.setItems(productsObservableList);

        centerVBox.getChildren().addAll(manageProductsText, productsTableView);

        leftVBox = new VBox();
        leftVBox.setAlignment(Pos.CENTER);
        leftVBox.setSpacing(15);
        leftVBox.setPadding(new Insets(20));

        searchTextField = UIHelperC.createStyledTextField("Product name");
        editBtn = UIHelperC.createStyledButton("Edit Product");
        addBtn = UIHelperC.createStyledButton("Add Shipment");

        leftVBox.getChildren().addAll(searchTextField, editBtn, addBtn);

        editBtn.setOnAction(e -> editAction());
        addBtn.setOnAction(e -> addShipment.showStage());

        // ✅ search filter
        searchTextField.textProperty().addListener((obs, oldV, newV) -> filterTable(newV));

        root.setCenter(centerVBox);
        root.setLeft(leftVBox);

        // ✅ load table first time
        refreshTable();
    }

    public BorderPane getRoot() {
        return root;
    }

    // =========================
    // Load from DB into table
    // =========================
    public void refreshTable() {
        try (Connection con = DBUtil.getConnection()) {
            if (con == null) return;

            ProductDAO dao = new ProductDAO();
            products = dao.getAllProducts(con);

            productsObservableList.setAll(products);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // Filter by name (and category)
    // =========================
    private void filterTable(String text) {
        if (text == null || text.trim().isEmpty()) {
            productsObservableList.setAll(products);
            return;
        }

        String q = text.trim().toLowerCase();
        ArrayList<Product> filtered = new ArrayList<>();

        for (Product p : products) {
            String n = p.getItemName() == null ? "" : p.getItemName().toLowerCase();
            String c = p.getCategoryName() == null ? "" : p.getCategoryName().toLowerCase();

            if (n.contains(q) || c.contains(q)) {
                filtered.add(p);
            }
        }

        productsObservableList.setAll(filtered);
    }

    private void editAction() {
        Product selected = productsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Please select a product to edit");
            return;
        }
        System.out.println("Edit: " + selected.getItemName());
    }
}
