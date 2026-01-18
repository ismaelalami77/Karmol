package ManagerView.ProductsManagement;

import Connection.ProductDAO;
import Connection.DBUtil;
import Connection.CategoryDAO;
import Product.Product;
import com.example.comp333finalproj.UIHelperC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;

public class AddShipment {
    private Runnable onSuccessRefresh;

    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private GridPane grid;
    private HBox buttonsHbox;
    private VBox centerVbox;

    private Text addShipmentText;
    private Button addShipmentButton, cancelButton;

    private Text productNameText, categoryNameText, priceText, quantityText;
    private TextField productNameTextField, priceTextField, quantityTextField;
    private ComboBox<String> categoryComboBox;

    private ObservableList<String> categoryList = FXCollections.observableArrayList();

    public AddShipment(Runnable onSuccessRefresh) {
        this.onSuccessRefresh = onSuccessRefresh;
        root = new BorderPane();

        centerVbox = new VBox();
        centerVbox.setAlignment(Pos.CENTER);
        centerVbox.setSpacing(15);

        addShipmentText = UIHelperC.createTitleText("Add Shipment");

        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        productNameText = UIHelperC.createInfoText("Name: ");
        productNameTextField = UIHelperC.createStyledTextField("name");

        categoryNameText = UIHelperC.createInfoText("Category: ");

        categoryComboBox = UIHelperC.createComboBox();
        categoryComboBox.setPromptText("Select Category");
        categoryComboBox.setPrefWidth(250);
        categoryComboBox.setEditable(true);
        loadCategoriesIntoComboBox();

        priceText = UIHelperC.createInfoText("Price: ");
        priceTextField = UIHelperC.createStyledTextField("price");

        quantityText = UIHelperC.createInfoText("quantity: ");
        quantityTextField = UIHelperC.createStyledTextField("quantity");

        grid.add(productNameText, 0, 0);
        grid.add(productNameTextField, 1, 0);

        grid.add(categoryNameText, 0, 1);
        grid.add(categoryComboBox, 1, 1);

        grid.add(priceText, 0, 2);
        grid.add(priceTextField, 1, 2);

        grid.add(quantityText, 0, 3);
        grid.add(quantityTextField, 1, 3);


        buttonsHbox = new HBox();
        buttonsHbox.setAlignment(Pos.CENTER);
        buttonsHbox.setSpacing(15);
        buttonsHbox.setPadding(new Insets(20));

        addShipmentButton = UIHelperC.createStyledButton("Add");
        cancelButton = UIHelperC.createStyledButton("Cancel");
        buttonsHbox.getChildren().addAll(addShipmentButton, cancelButton);

        centerVbox.getChildren().addAll(addShipmentText, grid, buttonsHbox);

        root.setCenter(centerVbox);
        stage = new Stage();
        scene = new Scene(root, 650, 500);
        stage.setScene(scene);
        stage.setTitle("Add Shipment");

        cancelButton.setOnAction(e -> stage.close());
        addShipmentButton.setOnAction(e -> addShipmentAction());

        productNameTextField.setOnAction(e -> categoryComboBox.requestFocus());
        categoryComboBox.setOnAction(e -> priceTextField.requestFocus());
        priceTextField.setOnAction(e -> quantityTextField.requestFocus());
        quantityTextField.setOnAction(e -> addShipmentAction());
    }

    private void addShipmentAction() {
        String name = productNameTextField.getText().trim();
        String categoryName = (categoryComboBox.getValue() == null) ? "" : categoryComboBox.getValue().trim().toLowerCase();
        String priceStr = priceTextField.getText().trim();
        String qtyStr = quantityTextField.getText().trim();

        if (name.isEmpty() || categoryName.isEmpty() || priceStr.isEmpty() || qtyStr.isEmpty()) {
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Missing Data");
            return;
        }

        double price;
        int qty;

        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException ex) {
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Invalid Price");
            priceTextField.requestFocus();
            return;
        }

        try {
            qty = Integer.parseInt(qtyStr);
        } catch (NumberFormatException ex) {
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Invalid Quantity");
            quantityTextField.requestFocus();
            return;
        }

        if (price <= 0) {
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Price must be greater than 0");
            return;
        }

        if (qty < 0){
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Quantity must be greater than 0");
        }

        ProductDAO productDAO = new ProductDAO();
        CategoryDAO categoryDAO = new CategoryDAO();

        try (Connection con = DBUtil.getConnection()) {
            if (con == null) {
                UIHelperC.showAlert(Alert.AlertType.ERROR, "DB Error");
                return;
            }


            int categoryId = categoryDAO.getOrCreateCategoryId(con, categoryName);


            Integer productId = productDAO.getProductIdByNameAndCategory(con, name, categoryId);

            boolean ok;
            if (productId != null) {
                ok = productDAO.addQuantityToExistingProduct(con, productId, qty);
            } else {
                Product p = new Product(0, name, categoryName, price);
                ok = productDAO.insertProductWithQuantity(con, p, categoryId, qty);
            }

            if (ok) {
                if (onSuccessRefresh != null) {
                    onSuccessRefresh.run();
                }
                UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Success");
                clearFields();
                stage.close();
                loadCategoriesIntoComboBox();
            } else {
                UIHelperC.showAlert(Alert.AlertType.ERROR, "Failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Error");
        }
    }


    private void clearFields(){
        productNameTextField.clear();
        categoryComboBox.setValue(null);
        priceTextField.clear();
        quantityTextField.clear();
    }

    public void showStage() {
        refreshCategories();
        stage.show();
    }

    private void loadCategoriesIntoComboBox() {
        categoryList.clear();

        try(Connection con = DBUtil.getConnection()){
            if (con == null) {
                return;
            }
            CategoryDAO categoryDAO = new CategoryDAO();

            var categories = categoryDAO.getAllCategories(con);

            for (var category : categories) {
                categoryList.add(category.getName());
            }
            categoryComboBox.setItems(categoryList);

        }catch (Exception e) {
            e.printStackTrace();
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Error loading Categories");
        }
    }

    public void refreshCategories(){
        loadCategoriesIntoComboBox();
    }
}
