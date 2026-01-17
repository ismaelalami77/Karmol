package ManagerView.ProductsManagement;

import Connection.DBUtil;
import Connection.CategoryDAO;
import Connection.ProductDAO;

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

public class EditProduct {
    private Runnable onSuccessRefresh;

    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private GridPane grid;
    private HBox buttonsHbox;
    private VBox centerVbox;


    private Text editProductText;
    private Button editButton, cancelButton;

    private Text productNameText, categoryNameText, priceText, quantityText;
    private TextField productNameTextField, priceTextField, quantityTextField;
    private ComboBox<String> categoryComboBox;

    private ObservableList<String> categoryList = FXCollections.observableArrayList();

    private Product selectedProduct;


    public EditProduct(Runnable onSuccessRefresh) {
        this.onSuccessRefresh = onSuccessRefresh;

        root = new BorderPane();

        centerVbox = new VBox();
        centerVbox.setAlignment(Pos.CENTER);
        centerVbox.setSpacing(15);

        editProductText = UIHelperC.createTitleText("Edit Product");

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

        editButton = UIHelperC.createStyledButton("Edit");
        cancelButton = UIHelperC.createStyledButton("Cancel");
        buttonsHbox.getChildren().addAll(editButton, cancelButton);

        centerVbox.getChildren().addAll(editProductText, grid, buttonsHbox);

        root.setCenter(centerVbox);
        stage = new Stage();
        scene = new Scene(root, 650, 500);
        stage.setScene(scene);
        stage.setTitle("Edit Shipment");

        cancelButton.setOnAction(e -> stage.close());
        editButton.setOnAction(e -> editAction());


        productNameTextField.setOnAction(e -> categoryComboBox.requestFocus());
        categoryComboBox.setOnAction(e -> priceTextField.requestFocus());
        priceTextField.setOnAction(e -> quantityTextField.requestFocus());
        quantityTextField.setOnAction(e -> editAction());
    }

    private void editAction() {
        if (selectedProduct == null) {
            UIHelperC.showAlert(Alert.AlertType.ERROR, "No product selected");
            return;
        }

        String name = productNameTextField.getText().trim();
        String categoryName = categoryComboBox.getValue().trim();
        String priceStr = priceTextField.getText().trim();
        String quantityStr = quantityTextField.getText().trim();

        if (name.isEmpty() || categoryName.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty()) {
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Please fill all the fields");
            return;
        }

        double price;
        int qty;
        try{
            price = Double.parseDouble(priceStr);
            qty = Integer.parseInt(quantityStr);
        }catch (Exception e){
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Price and quantity must be numbers");
            return;
        }

        if (price <= 0) {
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Price must be greater than 0");
            return;
        }

        if (qty < 0){
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Quantity must be greater than 0");
        }

        try(Connection con = DBUtil.getConnection()){
            if (con == null){
                return;
            }

            CategoryDAO categoryDAO = new CategoryDAO();
            int categoryId = categoryDAO.getCategoryIdByName(con, categoryName);
            if (categoryId == -1){
                UIHelperC.showAlert(Alert.AlertType.ERROR, "Category does not exist");
                return;
            }

            ProductDAO productDAO = new ProductDAO();
            boolean ok = productDAO.updateProduct(
                    con,
                    selectedProduct.getId(),
                    name,
                    categoryId,
                    price,
                    qty
            );
            if (ok){
                UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Product updated");

                if (onSuccessRefresh != null) {
                    onSuccessRefresh.run();
                }

                selectedProduct = null;

                stage.close();
            }else {
                UIHelperC.showAlert(Alert.AlertType.ERROR, "Error updating product");
            }
        }catch (Exception e){
            e.printStackTrace();
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Error updating product");
        }
    }

    public void showStage(Product selected) {
        this.selectedProduct = selected;

        refreshCategories();

        productNameTextField.setText(selected.getItemName());
        priceTextField.setText(String.valueOf(selected.getPricePerPiece()));
        quantityTextField.setText(String.valueOf(selected.getQuantity()));

        if (selected.getCategoryName() != null){
            categoryComboBox.getSelectionModel().select(selected.getCategoryName());
            categoryComboBox.setValue(selected.getCategoryName());
        }

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
