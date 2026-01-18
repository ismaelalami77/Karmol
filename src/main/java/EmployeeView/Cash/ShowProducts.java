package EmployeeView.Cash;

import Connection.DBUtil;
import Connection.ProductDAO;

import Product.Category;
import Product.Product;
import com.example.comp333finalproj.UIHelperC;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.util.ArrayList;

public class ShowProducts {
    private final Category category;
    private final Runnable onClose;
    private final Runnable onBack;
    private BorderPane root;
    private VBox centerVbox;
    private GridPane grid;
    private Text titleText;
    private Button cancelButton;

    public ShowProducts(Category category, Runnable onBack, Runnable onClose) {
        this.category = category;
        this.onBack = onBack;
        this.onClose = onClose;

        root = new BorderPane();

        centerVbox = new VBox();
        centerVbox.setAlignment(Pos.CENTER);
        centerVbox.setSpacing(15);

        titleText = UIHelperC.createTitleText(category.getName());

        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);

        cancelButton = UIHelperC.createStyledButton("Back");
        cancelButton.setOnAction(e -> onBack.run());


        centerVbox.getChildren().addAll(titleText, scrollPane, cancelButton);
        root.setCenter(centerVbox);

        loadProducts();
    }

    public BorderPane getRoot() {
        return root;
    }

    private void loadProducts() {
        grid.getChildren().clear();

        try (Connection con = DBUtil.getConnection()) {
            ProductDAO dao = new ProductDAO();

            int categoryID = category.getId();

            ArrayList<Product> products = dao.getProductsByCategoryId(con, categoryID);

            int col = 0;
            int row = 0;

            for (Product product : products) {
                String btnText = product.getItemName() + " - $" + product.getPricePerPiece();

                Button btn = UIHelperC.createStyledButton(btnText);
                btn.setPrefWidth(250);

                if (product.getQuantity() <= 0) {
                    btn.setDisable(true);
                    btn.setText(product.getItemName() + " - Out of Stock");
                } else {
                    btn.setOnAction(e -> addToCashTable(product));
                }

                grid.add(btn, col, row);

                col++;
                if (col == 2) {
                    col = 0;
                    row++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Error loading products");
        }
    }

    private void addToCashTable(Product selectedProduct) {

        // selectedProduct.getQuantity() here MUST be the DB stock quantity
        int stockQty = selectedProduct.getQuantity();

        // how many already in cart?
        int inCartQty = 0;
        for (Product p : CashView.products) {
            if (p.getId() == selectedProduct.getId()) {
                inCartQty = p.getQuantity();
                break;
            }
        }

        // block if cart reached stock
        if (inCartQty >= stockQty) {
            UIHelperC.showAlert(Alert.AlertType.WARNING,
                    "Not enough stock! Available: " + stockQty);

            return;
        }

        // add/increase in cart
        for (Product product : CashView.products) {
            if (product.getId() == selectedProduct.getId()) {
                product.setQuantity(product.getQuantity() + 1);
                CashView.refreshTable();
                return;
            }
        }

        Product copy = new Product(
                selectedProduct.getId(),
                selectedProduct.getItemName(),
                selectedProduct.getCategoryName(),
                selectedProduct.getPricePerPiece()
        );
        copy.setQuantity(1);

        CashView.products.add(copy);
        CashView.refreshTable();
    }


}
