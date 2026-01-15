package EmployeeView;

import Product.Category;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AddProductScene {
    private Stage stage;
    private Scene scene;
    private BorderPane root;


    public AddProductScene() {
        root = new BorderPane();

        ShowCategories showCategories = new ShowCategories(
                this::openProducts,
                this::closeStage
        );

        root.setCenter(showCategories.getRoot());

        stage = new Stage();
        scene = new Scene(root, 600, 500);
        stage.setScene(scene);
        stage.setTitle("Add Product");
    }

    private void openProducts(Category category) {
        ShowProducts showProducts = new ShowProducts(
                category,
                this::openCategories,
                this::closeStage
        );
        root.setCenter(showProducts.getRoot());
    }

    private void openCategories() {
        ShowCategories showCategories = new ShowCategories(
                this::openProducts,
                this::closeStage
        );
        root.setCenter(showCategories.getRoot());
    }

    private void closeStage() {
        stage.close();
    }

    public void showStage() {
        stage.show();
    }

    public void resetToCategories(){
        openCategories();
    }
}
