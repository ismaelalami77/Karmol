package EmployeeView;


import Connection.CategoryDAO;
import Connection.ProductDAO;
import Product.Category;
import Product.Product;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import Connection.DBUtil;

import java.sql.Connection;
import java.util.ArrayList;

public class AddProductScene {
    private Stage stage;
    private Scene scene;
    private BorderPane root;

    private GridPane grid;
    private Button backBtn;
    private Text titleText;

    private Integer selectedCategoryId = null;


    public AddProductScene() {
        root = new BorderPane();

        backBtn = new Button("Back");
        backBtn.setDisable(true);
        backBtn.setOnAction(e -> showCategories());

        titleText = new Text("Categories");

        HBox topBar = new HBox(10, backBtn, titleText);
        topBar.setPadding(new Insets(10));
        root.setRight(topBar);

        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToHeight(true);
        root.setCenter(scrollPane);


        stage = new Stage();
        scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);

        showCategories();

    }

    public void showStage() {
        stage.show();
    }

    private void showCategories() {
        selectedCategoryId = null;
        backBtn.setDisable(true);
        titleText.setText("Categories");
        grid.getChildren().clear();

        try (Connection con = DBUtil.getConnection()) {
            CategoryDAO dao = new CategoryDAO();
            ArrayList<Category> categories = dao.getAllCategories(con);

            int col = 0;
            int row = 0;
            int maxCols = 4;

            for (Category c : categories) {
                Button btn = new Button(c.getName());
                btn.setPrefWidth(130);

                btn.setOnAction(e -> showProducts(c.getId(), c.getName()));

                grid.add(btn, col, row);

                col++;

                if (col == maxCols) {
                    col = 0;
                    row++;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showProducts(int categoryId, String categoryName) {
        selectedCategoryId = categoryId;
        backBtn.setDisable(false);
        titleText.setText("Products - " + categoryName);
        grid.getChildren().clear();

        try(Connection con = DBUtil.getConnection()){
            ProductDAO dao = new ProductDAO();
            ArrayList<Product> products = dao.getProductsByCategoryId(con, categoryId);

            int col = 0;
            int row = 0;
            int maxCols = 4;

            for (Product p : products) {
                Button btn = new Button(p.getItemName() + "\n" + p.getPricePerPiece());
                btn.setPrefWidth(130);
                btn.setPrefHeight(60);

                btn.setOnAction(e -> {
                    boolean found = false;

                    for (Product existingProduct : CashView.products) {
                        if (existingProduct.getId() == p.getId()) {
                            existingProduct.setQuantity(p.getQuantity() + 1);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        CashView.products.add(p);
                    }

                    CashView.refreshTable();
                });

                grid.add(btn, col, row);

                col++;
                if (col == maxCols) {
                    col = 0;
                    row++;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
