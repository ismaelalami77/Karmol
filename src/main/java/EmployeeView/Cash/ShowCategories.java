package EmployeeView.Cash;

import Connection.DBUtil;
import Connection.CategoryDAO;

import Product.Category;
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
import java.util.function.Consumer;

public class ShowCategories {
    private BorderPane root;

    private VBox centerVbox;
    private GridPane grid;

    private Text titleText;

    private Button cancelButton;

    private final Consumer<Category> onCategorySelected;
    private final Runnable onCancel;

    public ShowCategories(Consumer<Category> onCategorySelected, Runnable onCancel) {
        this.onCategorySelected = onCategorySelected;
        this.onCancel = onCancel;

        root = new BorderPane();

        centerVbox = new VBox();
        centerVbox.setAlignment(Pos.CENTER);
        centerVbox.setSpacing(15);

        titleText = UIHelperC.createTitleText("Categories");

        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);

        cancelButton = UIHelperC.createStyledButton("Cancel");
        cancelButton.setOnAction(e -> onCancel.run());


        centerVbox.getChildren().addAll(titleText, scrollPane, cancelButton);
        root.setCenter(centerVbox);

        loadCategories();
    }

    private void loadCategories() {
        grid.getChildren().clear();

        try(Connection con = DBUtil.getConnection()){
            CategoryDAO dao = new CategoryDAO();
            ArrayList<Category> categories = dao.getAllCategories(con);

            int col = 0;
            int row = 0;

            for (Category category : categories) {
                Button btn = UIHelperC.createStyledButton(category.getName());
                btn.setPrefWidth(200);

                btn.setOnAction(e -> onCategorySelected.accept(category));

                grid.add(btn, col, row);
                col++;
                if (col == 2){
                    col = 0;
                    row++;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            UIHelperC.showAlert(Alert.AlertType.ERROR, "error");
        }
    }

    public BorderPane getRoot() {
        return root;
    }
}
